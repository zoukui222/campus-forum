package com.zwz.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: ljx
 * @Date: 2025/11/21 20:21
 */
@Data
public class CommentVO {
    private Long id;
    private Long postId;

    // 内容 (现在直接包含 "回复 @某某: ...")
    private String content;

    // 作者信息
    private Long userId;
    private String authorName;
    private String authorAvatar;

    // 仅保留 parentId，前端可能用来判断这是否是一条回复，或者用于点击"回复"按钮时传参
    private Long parentId;

    private Long childCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
