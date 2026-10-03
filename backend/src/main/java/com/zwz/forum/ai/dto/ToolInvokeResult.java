package com.zwz.forum.ai.dto;

/**
 * 一次工具调用的执行结果
 */
public record ToolInvokeResult(boolean success, String content, long durationMs) {
}
