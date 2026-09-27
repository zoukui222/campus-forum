package com.zwz.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zwz.forum.common.exception.BusinessException;
import com.zwz.forum.dto.LoginRequest;
import com.zwz.forum.dto.LoginResult;
import com.zwz.forum.dto.RegisterRequest;
import com.zwz.forum.entity.User;
import com.zwz.forum.mapper.UserMapper;
import com.zwz.forum.service.AuthService;
import com.zwz.forum.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: ljx
 * @Date: 2025/11/21 14:25
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public void register(RegisterRequest req) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.username()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建新用户
        User user = new User();
        user.setUsername(req.username());
        user.setNickname(req.nickname());
        // 密码加密存储
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole("USER");
        user.setScore(0);
        // 默认头像 (可以用一个随机头像API，或者放一张默认图)
        user.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");

        userMapper.insert(user);
    }

    public LoginResult login(LoginRequest req) {
        // 1. 查用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.username()));

        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        // 2. 校验密码 (明文 vs 密文)
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        // 3. 生成 Token
        return new LoginResult(jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole()),user);
    }
}
