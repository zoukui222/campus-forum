package com.zwz.forum.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: ljx
 * @Date: 2025/11/24 15:33
 */
@Data
@Builder
public class UnreadCountVO {
    // 总未读数
    private Long total;

    // 私信未读数
    private Long chat;

    // 评论/回复未读数
    private Long comment;

    // 系统通知未读数
    private Long system;
}
