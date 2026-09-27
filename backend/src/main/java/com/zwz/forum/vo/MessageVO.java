package com.zwz.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: ljx
 * @Date: 2025/11/23 18:41
 */
@Data
public class MessageVO {
    private Long id;
    private Long fromId;
    private String fromNickname;
    private String fromAvatar;
    private String content; // 消息内容
    private String type;    // CHAT(私信) 或 COMMENT(评论回复)
    private Boolean isRead; // 是否已读

    // 如果是评论通知，可能需要跳转到帖子，这里存一下 postId (需要在 Entity 里存 content 为 json 或者 关联ID，简单起见存 content 里)
    // MVP 简化：如果是 COMMENT 类型，content 格式定为： "postId:文章标题" 或者前端直接解析

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
