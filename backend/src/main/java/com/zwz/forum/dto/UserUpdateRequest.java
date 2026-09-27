package com.zwz.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: ljx
 * @Date: 2025/11/26 13:42
 */
@Data
public class UserUpdateRequest {
    @NotBlank(message = "昵称不能为空")
    @Length(min = 2, max = 10, message = "昵称长度需在2-10个字符之间")
    private String nickname;

    private String avatar; // 头像URL

    private String email;  // 邮箱 (可选)
}
