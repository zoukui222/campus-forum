package com.zwz.forum.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI Agent 配置项（对应 application.yaml 中的 ai.*）
 * 密钥不入版本库：本地写在 application-local.yaml，部署环境注入环境变量 AI_API_KEY。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 模型服务地址（DeepSeek 兼容 OpenAI Chat Completions 协议） */
    private String baseUrl = "https://api.deepseek.com";

    /** 密钥 */
    private String apiKey;

    /** 模型名 */
    private String model = "deepseek-chat";

    /** 建连超时(秒) */
    private Integer connectTimeout = 10;

    /** 单次请求超时(秒)：流式响应期间整体上限 */
    private Integer requestTimeout = 120;

    /** 单次对话最多允许的工具调用轮次，防止模型反复调用同一工具 */
    private Integer maxIterations = 5;

    /** 采样温度 */
    private Double temperature = 0.3;

    /** 单个工具返回给模型的最大字符数，超出截断，避免上下文爆炸 */
    private Integer toolResultLimit = 4000;

    /** 是否把工具调用明细写入审计表 */
    private Boolean auditEnabled = true;
}
