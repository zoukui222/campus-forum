package com.zwz.forum.ai.controller;

import com.zwz.forum.ai.core.AgentContext;
import com.zwz.forum.ai.core.SseAgentListener;
import com.zwz.forum.ai.service.AiAgentService;
import com.zwz.forum.common.Result;
import com.zwz.forum.entity.User;
import com.zwz.forum.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;

/**
 * AI Agent 接口
 *
 * 为什么是 GET + SseEmitter 而不是 WebSocket：SSE 是单向的（服务端→浏览器），
 * 正好匹配「模型一直吐字、前端只负责显示」的场景，且 SseEmitter 是 Spring MVC 自带，零新依赖。
 *
 * 鉴权沿用项目既有的方式：JwtAuthenticationFilter 从 Authorization: Bearer 解析并写入 SecurityContext，
 * 所以本接口天然受 Spring Security 的 anyRequest().authenticated() 保护，无需额外配置。
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAgentController {

    /** 单次对话最长挂起时间；给个上限避免连接泄漏 */
    private static final long SSE_TIMEOUT_MS = 300000L;

    private final AiAgentService aiAgentService;
    private final UserMapper userMapper;
    private final ThreadPoolTaskExecutor aiAgentExecutor;

    /**
     * 流式对话。事件类型：meta / tool_call / tool_result / content / done / error
     */
    @GetMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(@RequestParam("message") String message) {
        // 关键：身份快照必须在当前请求线程里取。
        // SecurityContextHolder 默认是 ThreadLocal 策略，一旦切到 Agent 线程池就取不到了，
        // 所以这里先物化成 AgentContext，作为参数显式传下去。
        AgentContext context = currentContext();

        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        final String question = message == null ? "" : message.trim();

        if (question.isEmpty()) {
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data("{\"message\":\"提问内容不能为空\"}", MediaType.APPLICATION_JSON));
                emitter.complete();
            } catch (Exception ignored) {
                // 空提问直接结束，无需额外处理
            }
            return emitter;
        }

        emitter.onTimeout(() -> {
            log.warn("AI 对话超时: userId={}", context.getUserId());
            emitter.complete();
        });

        log.info("收到 AI 提问: userId={}, role={}, question={}", context.getUserId(), context.getRole(), question);

        aiAgentExecutor.execute(() -> aiAgentService.run(question, context, new SseAgentListener(emitter)));

        return emitter;
    }

    /** 查看当前账号可见的 Agent 配置（模型、已注册/可用工具数） */
    @GetMapping("/info")
    public Result<Object> info() {
        return Result.success(aiAgentService.describeTools(currentContext()));
    }

    /** 从当前请求的 SecurityContext 构造 AgentContext（含昵称，便于提示词里称呼用户） */
    private AgentContext currentContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            return new AgentContext(null, null, null, null);
        }
        Long userId = (Long) authentication.getPrincipal();

        String role = "USER";
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities != null && !authorities.isEmpty()) {
            role = authorities.iterator().next().getAuthority().replace("ROLE_", "");
        }

        String username = null;
        String nickname = null;
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                username = user.getUsername();
                nickname = user.getNickname();
            }
        } catch (Exception e) {
            log.warn("查询当前用户信息失败: {}", e.getMessage());
        }
        return new AgentContext(userId, username, nickname, role);
    }
}
