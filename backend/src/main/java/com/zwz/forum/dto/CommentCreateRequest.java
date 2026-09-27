package com.zwz.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: ljx
 * @Date: 2025/11/21 20:19
 */
public record CommentCreateRequest(@NotNull(message = "必须指定帖子ID") Long postId,
                                   @NotBlank(message = "评论内容不能为空") String content,
                                   Long parentId
) {}
