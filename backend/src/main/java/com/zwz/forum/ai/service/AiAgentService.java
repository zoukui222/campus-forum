package com.zwz.forum.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zwz.forum.ai.client.DeepSeekClient;
import com.zwz.forum.ai.config.AiProperties;
import com.zwz.forum.ai.core.AgentContext;
import com.zwz.forum.ai.core.AgentEventListener;
import com.zwz.forum.ai.dto.AiMessage;
import com.zwz.forum.ai.dto.AiToolCall;
import com.zwz.forum.ai.dto.StreamResult;
import com.zwz.forum.ai.dto.ToolInvokeResult;
import com.zwz.forum.ai.entity.AiChatLog;
import com.zwz.forum.ai.exception.ClientDisconnectedException;
import com.zwz.forum.ai.mapper.AiChatLogMapper;
import com.zwz.forum.ai.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 主循环（ReAct 风格的 function calling 循环）
 *
 * 一轮完整流程：
 *   模型思考 → 若决定调用工具，则挂起等待 → 执行工具 → 把结果作为 tool 消息塞回对话历史 → 再次请求模型 → …
 *   直到模型不再要求调用工具、直接给出文本回答为止。
 *
 * 两道硬约束：
 *   1) 最大轮次熔断：模型可能反复调同一个工具（尤其查询结果为空时），必须有上限；
 *   2) 收口兜底：触达上限时再补一次「不带工具」的请求，逼模型用已有信息作答，避免给用户空白回复。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int QUESTION_LIMIT = 2000;

    private final DeepSeekClient deepSeekClient;
    private final ToolRegistry toolRegistry;
    private final AiProperties properties;
    private final AiChatLogMapper chatLogMapper;

    /**
     * 执行一次完整的 Agent 对话。方法本身不抛异常，所有异常都通过 listener 上报，
     * 保证 SSE 连接一定能收到结束事件（前端不会一直转圈）。
     */
    public void run(String question, AgentContext context, AgentEventListener listener) {
        // 关键一步：把身份「种」回当前线程的 SecurityContext。
        // Agent 跑在独立线程池里，SecurityContextHolder（MODE_THREADLOCAL）原本是空的，
        // 而项目下游的 service / 切面全都通过 SecurityUtils 取用户 —— 不重建的话，
        // 任何调用 SecurityUtils.getUserId() 的地方都会抛 BusinessException(401)。
        // 在这里重建一次，下游代码完全无感，不用为 AI 场景改造任何既有 service。
        bindSecurityContext(context);

        long start = System.currentTimeMillis();
        int chatLogId = createChatLog(question, context);

        int promptTokens = 0;
        int completionTokens = 0;
        int iterations = 0;
        StringBuilder answer = new StringBuilder();
        String status = "SUCCESS";
        String errorMsg = null;

        try {
            List<AiMessage> messages = new ArrayList<>();
            messages.add(AiMessage.system(buildSystemPrompt(context)));
            messages.add(AiMessage.user(question));

            ArrayNode tools = toolRegistry.buildToolSpec(context);
            listener.onMeta(chatLogId, properties.getModel(), toolNamesOf(tools));

            int maxRounds = properties.getMaxIterations() == null ? 5 : properties.getMaxIterations();

            for (int round = 0; round < maxRounds; round++) {
                StreamResult result = deepSeekClient.chatStream(messages, tools, listener::onContent);
                promptTokens += result.getPromptTokens();
                completionTokens += result.getCompletionTokens();

                if (!result.hasToolCalls()) {
                    answer.append(result.getContent());
                    break;
                }

                iterations++;

                // 关键：把模型这一轮的「我要调这些工具」原样写回历史，
                // 否则下一轮请求里出现 tool 结果却找不到对应的 tool_calls，接口会直接报 400
                AiMessage assistantMessage = AiMessage.assistant(result.getContent());
                assistantMessage.setToolCalls(result.getToolCalls());
                messages.add(assistantMessage);

                for (AiToolCall call : result.getToolCalls()) {
                    listener.onToolCall(call.getName(), call.getArguments());
                    ToolInvokeResult invokeResult = toolRegistry.invoke(call, context, (long) chatLogId);
                    listener.onToolResult(call.getName(), invokeResult.success(),
                            invokeResult.content(), invokeResult.durationMs());
                    messages.add(AiMessage.tool(call.getId(), call.getName(), invokeResult.content()));
                }

                if (round == maxRounds - 1) {
                    status = "ABORTED";
                    errorMsg = "达到最大工具调用轮次 " + maxRounds;
                    log.warn("Agent 触达工具轮次上限: chatLogId={}, question={}", chatLogId, question);
                    // 收口：去掉工具再问一次，让模型基于已经拿到的事实作答
                    StreamResult closing = deepSeekClient.chatStream(messages, null, listener::onContent);
                    promptTokens += closing.getPromptTokens();
                    completionTokens += closing.getCompletionTokens();
                    answer.append(closing.getContent());
                }
            }
        } catch (ClientDisconnectedException disconnected) {
            log.info("客户端已断开，终止 Agent 执行: chatLogId={}", chatLogId);
            return;
        } catch (Exception e) {
            status = "ERROR";
            errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error("Agent 执行失败: chatLogId={}, question={}", chatLogId, question, e);
        } finally {
            // 必须清理：线程池里的线程会被复用，残留的 SecurityContext 会污染下一个请求
            SecurityContextHolder.clearContext();
        }

        long duration = System.currentTimeMillis() - start;
        String finalAnswer = answer.length() > 0 ? answer.toString() : "抱歉，本次没有生成有效回答，请换个说法再试一次。";

        finishChatLog(chatLogId, finalAnswer, iterations, promptTokens, completionTokens, duration, status, errorMsg);

        if ("ERROR".equals(status)) {
            listener.onError(errorMsg, chatLogId);
        } else {
            listener.onDone(iterations, promptTokens, completionTokens, duration, chatLogId);
        }
    }

    /**
     * 在当前线程重建 SecurityContext。
     * 注意授权前缀必须是 ROLE_ 开头，Spring Security 的 hasRole/hasAnyRole 才能匹配。
     */
    private void bindSecurityContext(AgentContext context) {
        if (context == null || context.getUserId() == null) {
            return;
        }
        String role = context.getRole() == null ? "USER" : context.getRole();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                context.getUserId(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 系统提示词。核心是把「不许编造」写成硬规则 —— 工具型 Agent 最大的翻车点就是模型宁可编一个
     * 帖子 ID 也不肯说「没查到」，必须在提示词层面压住。
     */
    private String buildSystemPrompt(AgentContext context) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是校园论坛的智能助手，可以通过调用工具查询论坛真实数据。\n\n");
        builder.append("必须遵守的规则：\n");
        builder.append("1. 凡是涉及论坛帖子、板块、统计、用户的问题，必须先调用工具拿到真实数据再回答；");
        builder.append("严禁凭空编造帖子标题、帖子 ID、浏览量或任何数字。\n");
        builder.append("2. 引用帖子时给出标题和 id，方便用户点击定位。\n");
        builder.append("3. 工具返回为空或失败时，如实说明查询不到，不要用想象的内容填补。\n");
        builder.append("4. 与论坛数据无关的问题（闲聊、通用技术问答）可以直接回答，但不要伪装成论坛内容。\n");
        builder.append("5. 用简体中文回答，直接、简洁，不要客套话和免责声明。\n");
        builder.append("6. 用户明确要求执行操作时，必须调用对应工具真正完成，不要只做文字说明或反问确认：");
        builder.append("要求发帖就用 create_post 发布（先用 list_boards 确认板块 ID）；");
        builder.append("版主/管理员要求置顶或取消置顶就用 top_post 执行。\n");
        builder.append("7. 操作前为确认目标存在的查询最多一次（例如置顶前查一次帖子），不要反复查证。\n");
        builder.append("8. 列举多条内容时用简短的 Markdown 列表（每行以 - 开头），不要使用 Markdown 表格，前端聊天框不支持表格渲染。\n");
        builder.append("9. 全程使用简体中文，包括你开口的第一句话，不要输出英文思考。\n");

        if (context != null && context.isAuthenticated()) {
            builder.append("\n当前登录用户：").append(context.displayName())
                    .append("，角色：").append(context.getRole()).append("。");
            if (context.isModerator()) {
                builder.append("该用户是版主/管理员，可以调用置顶等管理类工具。");
            } else {
                builder.append("该用户是普通用户，没有置顶等管理权限；若用户提出管理操作，请说明需要版主权限。");
            }
        } else {
            builder.append("\n当前用户未登录（游客），只能查询公开信息。");
        }
        return builder.toString();
    }

    private List<String> toolNamesOf(ArrayNode tools) {
        List<String> names = new ArrayList<>();
        if (tools == null) {
            return names;
        }
        for (JsonNode node : tools) {
            JsonNode function = node.get("function");
            if (function != null && function.get("name") != null) {
                names.add(function.get("name").asText());
            }
        }
        return names;
    }

    /** 先落一条 RUNNING 记录拿到主键，工具审计日志才有 chatLogId 可关联 */
    private int createChatLog(String question, AgentContext context) {
        try {
            AiChatLog record = new AiChatLog();
            if (context != null) {
                record.setUserId(context.getUserId());
                record.setUsername(context.getUsername());
                record.setNickname(context.getNickname());
                record.setRole(context.getRole());
            }
            record.setQuestion(abbreviate(question, QUESTION_LIMIT));
            record.setAnswer("");
            record.setIterations(0);
            record.setModel(properties.getModel());
            record.setPromptTokens(0);
            record.setCompletionTokens(0);
            record.setDurationMs(0L);
            record.setStatus("RUNNING");
            record.setCreateTime(LocalDateTime.now().format(TIME_FORMAT));
            chatLogMapper.insert(record);
            return record.getId() == null ? -1 : record.getId().intValue();
        } catch (Exception e) {
            log.warn("写入会话记录失败，本次对话将不留痕: {}", e.getMessage());
            return -1;
        }
    }

    private void finishChatLog(int chatLogId, String answer, int iterations, int promptTokens,
                               int completionTokens, long duration, String status, String errorMsg) {
        if (chatLogId <= 0) {
            return;
        }
        try {
            AiChatLog record = new AiChatLog();
            record.setId((long) chatLogId);
            record.setAnswer(answer);
            record.setIterations(iterations);
            record.setPromptTokens(promptTokens);
            record.setCompletionTokens(completionTokens);
            record.setDurationMs(duration);
            record.setStatus(status);
            record.setErrorMsg(errorMsg);
            chatLogMapper.updateById(record);
        } catch (Exception e) {
            log.warn("回填会话记录失败: {}", e.getMessage());
        }
    }

    private static String abbreviate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }

    /** 供健康检查/前端展示当前账号可见的 Agent 配置 */
    public Map<String, Object> describeTools(AgentContext context) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("model", properties.getModel());
        info.put("registered", toolRegistry.size());
        ArrayNode spec = toolRegistry.buildToolSpec(context);
        info.put("available", spec == null ? 0 : spec.size());
        return info;
    }
}
