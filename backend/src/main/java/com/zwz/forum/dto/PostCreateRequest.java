package com.zwz.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: ljx
 * @Date: 2025/11/21 17:32
 */
public record PostCreateRequest(
        @NotBlank(message = "标题不能为空") String title,
        @NotBlank(message = "内容不能为空") String content, // Markdown 源码
        @NotNull(message = "必须选择板块") Integer boardId
) {}