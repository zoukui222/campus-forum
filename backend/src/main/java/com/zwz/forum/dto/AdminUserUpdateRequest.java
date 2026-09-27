package com.zwz.forum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员修改用户信息请求
 * @author ljx
 * @since 2026-02-10
 */
@Data
public class AdminUserUpdateRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    private String username;
    
    private String nickname;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private Integer score;
    
    private String role;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    
    private String avatar;
}