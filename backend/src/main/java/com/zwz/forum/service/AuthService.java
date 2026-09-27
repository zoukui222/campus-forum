package com.zwz.forum.service;

import com.zwz.forum.dto.LoginRequest;
import com.zwz.forum.dto.LoginResult;
import com.zwz.forum.dto.RegisterRequest;

/**
 * @Author: ljx
 * @Date: 2025/11/21 14:24
 */
public interface AuthService {
    void register(RegisterRequest req);
    LoginResult login(LoginRequest req);
}
