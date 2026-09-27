package com.zwz.forum.controller;

import com.zwz.forum.common.Result;
import com.zwz.forum.dto.LoginRequest;
import com.zwz.forum.dto.LoginResult;
import com.zwz.forum.dto.RegisterRequest;
import com.zwz.forum.entity.User;
import com.zwz.forum.mapper.UserMapper;
import com.zwz.forum.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author: ljx
 * @Date: 2025/11/21 14:23
 */
@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserMapper userMapper;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest request) {
        LoginResult loginResult = authService.login(request);
        User user = loginResult.getUser();
        user.setPassword(null);
        String token = loginResult.getToken();
        return Result.success(Map.of("token", token,"user", user));
    }
}
