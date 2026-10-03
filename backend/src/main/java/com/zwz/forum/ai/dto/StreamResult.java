package com.zwz.forum.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 一次流式响应的聚合结果
 */
@Data
public class StreamResult {

    /** 文本正文（流式分片已拼接） */
    private String content = "";
    /** 模型要求调用的工具（为空表示本轮是最终回答） */
    private List<AiToolCall> toolCalls;
    /** stop / tool_calls / length */
    private String finishReason;
    private int promptTokens;
    private int completionTokens;

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
