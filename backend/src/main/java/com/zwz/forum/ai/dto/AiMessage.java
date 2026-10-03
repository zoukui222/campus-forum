package com.zwz.forum.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 对话消息（对齐 OpenAI / DeepSeek Chat Completions 的 message 结构）
 * role 取值：system / user / assistant / tool
 */
@Data
public class AiMessage {

    private String role;
    private String content;
    /** assistant 消息中：模型决定要调用的工具列表 */
    private List<AiToolCall> toolCalls;
    /** role=tool 时：回填的是哪一次工具调用 */
    private String toolCallId;
    /** role=tool 时：工具名 */
    private String name;

    public AiMessage() {
    }

    public AiMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static AiMessage system(String content) {
        return new AiMessage("system", content);
    }

    public static AiMessage user(String content) {
        return new AiMessage("user", content);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage("assistant", content);
    }

    public static AiMessage tool(String toolCallId, String name, String content) {
        AiMessage message = new AiMessage("tool", content);
        message.setToolCallId(toolCallId);
        message.setName(name);
        return message;
    }
}
