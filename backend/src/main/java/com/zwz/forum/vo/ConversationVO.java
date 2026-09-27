package com.zwz.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: ljx
 * @Date: 2025/11/24 14:25
 */
@Data
public class ConversationVO {
    // 聊天对象的身份信息
    private Long userId;
    private String nickname;
    private String avatar;

    // 摘要信息 (用于列表展示)
    private String latestMessage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestTime;

    // 该会话的未读消息数
    private Integer unreadCount;
}
