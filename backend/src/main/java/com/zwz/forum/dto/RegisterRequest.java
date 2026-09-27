package com.zwz.forum.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @Author: ljx
 * @Date: 2025/11/21 14:13
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        @NotBlank(message = "昵称不能为空") String nickname
) {}