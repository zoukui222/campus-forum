package com.zwz.forum.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: ljx
 * @Date: 2025/11/30 16:29
 */
public record PasswordUpdateRequest(
        @NotBlank(message = "旧密码不能为空")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        @Length(min = 6, max = 20, message = "密码长度需在6-20位之间")
        String newPassword,

        @NotBlank(message = "确认密码不能为空")
        String confirmPassword
) {}
