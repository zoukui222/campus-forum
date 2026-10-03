package com.zwz.forum.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zwz.forum.ai.config.AiProperties;
import com.zwz.forum.ai.dto.AiMessage;
import com.zwz.forum.ai.dto.AiToolCall;
import com.zwz.forum.ai.dto.StreamResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DeepSeek 流式对话客户端
 *
 * 用 JDK 11+ 自带的 java.net.http.HttpClient 配合 BodyHandlers.ofLines()，
 * 响应体以「行流」的形式增量返回，天然适配 SSE 的逐行协议，无需自己管 BufferedReader 与关闭。
 * （本项目 Java 17，可以直接用；JDK8 项目就只能退回 HttpURLConnection 手撸 InputStream。）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 文本增量回调：每收到一段正文就往外推一次，实现流式输出 */
    public interface DeltaListener {
        void onContent(String delta);
    }

    private final AiProperties properties;

    private HttpClient httpClient;

    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeout()))
                .build();
    }

    /**
     * 发起一次流式对话，返回聚合后的结果（正文 + 待执行的工具调用）
     *
     * @param messages 完整对话历史（含上一轮的工具执行结果）
     * @param tools    OpenAI 格式的工具声明，无工具传 null
     * @param listener 正文增量回调，可为 null
     */
    public StreamResult chatStream(List<AiMessage> messages, ArrayNode tools, DeltaListener listener)
            throws IOException, InterruptedException {

        if (properties.getApiKey() == null || properties.getApiKey().trim().isEmpty()) {
            throw new IllegalStateException("AI 服务未配置密钥：请在 application-local.yaml 中设置 ai.api-key，或注入环境变量 AI_API_KEY");
        }

        String payload = MAPPER.writeValueAsString(buildRequestBody(messages, tools));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/chat/completions"))
                .timeout(Duration.ofSeconds(properties.getRequestTimeout()))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "text/event-stream")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
        if (response.statusCode() != 200) {
            String error = response.body().collect(Collectors.joining("\n"));
            log.error("DeepSeek 接口返回异常: status={}, body={}", response.statusCode(), error);
            throw new IllegalStateException("AI 服务返回异常状态 " + response.statusCode() + "：" + abbreviate(error, 300));
        }
        return parseSse(response.body(), listener);
    }

    /**
     * 构造请求体。
     * 注意 assistant 消息里若带 tool_calls，content 需要显式置 null（模型要求字段存在），
     * 否则部分网关会返回 400。
     */
    private ObjectNode buildRequestBody(List<AiMessage> messages, ArrayNode tools) {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", properties.getModel());
        body.put("stream", true);
        body.put("temperature", properties.getTemperature());

        ArrayNode messageArray = body.putArray("messages");
        for (AiMessage message : messages) {
            ObjectNode node = messageArray.addObject();
            node.put("role", message.getRole());

            if ("tool".equals(message.getRole())) {
                node.put("content", message.getContent() == null ? "" : message.getContent());
                node.put("tool_call_id", message.getToolCallId());
                if (message.getName() != null) {
                    node.put("name", message.getName());
                }
                continue;
            }

            if (message.getContent() == null) {
                node.putNull("content");
            } else {
                node.put("content", message.getContent());
            }

            if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
                ArrayNode toolArray = node.putArray("tool_calls");
                for (AiToolCall call : message.getToolCalls()) {
                    ObjectNode callNode = toolArray.addObject();
                    callNode.put("id", call.getId());
                    callNode.put("type", "function");
                    ObjectNode function = callNode.putObject("function");
                    function.put("name", call.getName());
                    function.put("arguments", call.getArguments() == null ? "{}" : call.getArguments());
                }
            }
        }

        if (tools != null && !tools.isEmpty()) {
            body.set("tools", tools);
        }
        return body;
    }

    /**
     * 解析 SSE 数据流。
     *
     * 重点：tool_calls 在流式响应里按 index 分片下发 —— 第一片带 id 与 function.name，
     * 后续片只带 function.arguments 的字符串片段，必须按 index 聚合并把片段拼起来，
     * 拼完才是合法 JSON。这是流式 function calling 最容易踩的坑。
     */
    private StreamResult parseSse(Stream<String> lines, DeltaListener listener) {
        StringBuilder content = new StringBuilder();
        Map<Integer, AiToolCall> callMap = new LinkedHashMap<>();
        Map<Integer, StringBuilder> argBuffer = new LinkedHashMap<>();
        String finishReason = null;
        int promptTokens = 0;
        int completionTokens = 0;

        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line == null || line.isEmpty() || !line.startsWith("data:")) {
                continue; // 空行分隔符、event:、:keep-alive 注释行一律跳过
            }
            String data = line.substring(5).trim();
            if ("[DONE]".equals(data)) {
                break;
            }

            JsonNode node;
            try {
                node = MAPPER.readTree(data);
            } catch (Exception parseError) {
                log.warn("忽略无法解析的 SSE 分片: {}", abbreviate(data, 200));
                continue;
            }

            JsonNode usage = node.get("usage");
            if (usage != null && !usage.isNull()) {
                promptTokens = usage.path("prompt_tokens").asInt(promptTokens);
                completionTokens = usage.path("completion_tokens").asInt(completionTokens);
            }

            JsonNode choices = node.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                continue;
            }
            JsonNode choice = choices.get(0);

            JsonNode reason = choice.get("finish_reason");
            if (reason != null && !reason.isNull()) {
                finishReason = reason.asText();
            }

            JsonNode delta = choice.get("delta");
            if (delta == null || delta.isNull()) {
                continue;
            }

            JsonNode contentNode = delta.get("content");
            if (contentNode != null && !contentNode.isNull()) {
                String text = contentNode.asText();
                if (text != null && !text.isEmpty()) {
                    content.append(text);
                    if (listener != null) {
                        listener.onContent(text);
                    }
                }
            }

            JsonNode toolCalls = delta.get("tool_calls");
            if (toolCalls != null && toolCalls.isArray()) {
                for (JsonNode toolCall : toolCalls) {
                    int index = toolCall.path("index").asInt(0);
                    AiToolCall accumulated = callMap.computeIfAbsent(index, k -> {
                        argBuffer.put(k, new StringBuilder());
                        return new AiToolCall();
                    });
                    JsonNode idNode = toolCall.get("id");
                    if (idNode != null && !idNode.isNull() && accumulated.getId() == null) {
                        accumulated.setId(idNode.asText());
                    }
                    JsonNode function = toolCall.get("function");
                    if (function != null && !function.isNull()) {
                        JsonNode nameNode = function.get("name");
                        if (nameNode != null && !nameNode.isNull() && accumulated.getName() == null) {
                            accumulated.setName(nameNode.asText());
                        }
                        JsonNode argsNode = function.get("arguments");
                        if (argsNode != null && !argsNode.isNull()) {
                            argBuffer.get(index).append(argsNode.asText());
                        }
                    }
                }
            }
        }

        List<AiToolCall> toolCalls = new ArrayList<>();
        for (Map.Entry<Integer, AiToolCall> entry : callMap.entrySet()) {
            AiToolCall call = entry.getValue();
            StringBuilder buffer = argBuffer.get(entry.getKey());
            call.setArguments(buffer == null ? "{}" : buffer.toString());
            toolCalls.add(call);
        }

        StreamResult result = new StreamResult();
        result.setContent(content.toString());
        result.setToolCalls(toolCalls);
        result.setFinishReason(finishReason);
        result.setPromptTokens(promptTokens);
        result.setCompletionTokens(completionTokens);
        return result;
    }

    private static String abbreviate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
