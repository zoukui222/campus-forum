package com.zwz.forum.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 工具调用审计日志
 * 模型每一次工具调用都落一条：调了什么、参数、返回、耗时、成功与否。
 * 这是 Agent 可观测性的基础 —— 出问题时能直接回答「它到底查了什么、看到的是什么」。
 */
@Data
@TableName("ai_tool_call_log")
public class AiToolCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联的会话记录 ID */
    private Long chatLogId;
    private String toolName;
    private String arguments;
    private String result;
    /** 1=成功 0=失败 */
    private Integer success;
    private String errorMsg;
    private Long durationMs;
    private String createTime;
}
