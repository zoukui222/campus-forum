package com.zwz.forum.dto;

import com.zwz.forum.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: ljx
 * @Date: 2025/11/22 17:23
 */
@Data
@AllArgsConstructor
public class LoginResult {
    String token;
    User user;
}
