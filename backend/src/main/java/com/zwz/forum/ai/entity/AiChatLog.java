package com.zwz.forum.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 会话记录
 * 每次提问存一条：记录本轮问答、工具调用轮次与 token 消耗，用于回溯与成本核算。
 */
@Data
@TableName("ai_chat_log")
public class AiChatLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 提问用户 ID */
    private Long userId;
    private String username;
    private String nickname;
    /** 提问时角色 USER/MODERATOR/ADMIN */
    private String role;
    private String question;
    private String answer;
    /** 本轮实际发生的工具调用轮次 */
    private Integer iterations;
    private String model;
    private Integer promptTokens;
    private Integer completionTokens;
    /** 端到端耗时(ms) */
    private Long durationMs;
    /** RUNNING / SUCCESS / ABORTED / ERROR */
    private String status;
    private String errorMsg;
    private String createTime;
}
