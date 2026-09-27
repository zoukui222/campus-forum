package com.zwz.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: ljx
 * @Date: 2025/11/23 18:40
 */
public record MessageSendRequest(
        @NotNull(message = "接收者ID不能为空") Long toUserId,
        @NotBlank(message = "内容不能为空") String content
) {}