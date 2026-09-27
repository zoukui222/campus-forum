package com.zwz.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: ljx
 * @Date: 2025/11/21 17:32
 */
@Data
public class PostVO {
    private Long id;
    private String title;
    private String content; // 列表页通常只显示摘要，详情页显示全文
    private Integer boardId;
    private String boardName; // 板块名称

    // 作者信息
    private Long userId;
    private String authorName;
    private String authorAvatar;

    // 统计信息
    private Integer viewCount;
    private Integer replyCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}