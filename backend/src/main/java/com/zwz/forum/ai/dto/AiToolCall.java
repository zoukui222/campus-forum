package com.zwz.forum.ai.dto;

import lombok.Data;

/**
 * 一次工具调用请求
 * 流式响应中，arguments 是分片到达的，必须按 index 聚合后才是完整 JSON。
 */
@Data
public class AiToolCall {

    /** 模型分配的唯一 ID，回填工具结果时必须原样带回 */
    private String id;
    /** 工具名 */
    private String name;
    /** 参数（JSON 字符串） */
    private String arguments;
}
