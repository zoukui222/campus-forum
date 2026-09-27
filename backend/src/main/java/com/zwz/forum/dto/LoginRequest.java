package com.zwz.forum.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @Author: ljx
 * @Date: 2025/11/21 14:12
 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password
) {}