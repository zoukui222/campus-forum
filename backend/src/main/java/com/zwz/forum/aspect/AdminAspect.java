package com.zwz.forum.aspect;

import com.zwz.forum.common.annotation.RequireAdmin;
import com.zwz.forum.common.exception.BusinessException;
import com.zwz.forum.entity.User;
import com.zwz.forum.mapper.UserMapper;
import com.zwz.forum.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @Author: ljx
 * @Date: 2025/11/27 15:29
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAspect {
    private final UserMapper userMapper;
    @Before("@annotation(requireAdmin)")
    public void checkAdminPermission(JoinPoint joinPoint, RequireAdmin requireAdmin) {
        Long userId = SecurityUtils.getUserId(); // 获取当前用户

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户未登录或不存在");
        }

        // 核心鉴权逻辑
        if (!"ADMIN".equals(user.getRole()) && !"MODERATOR".equals(user.getRole())) {
            log.warn("用户 {} 试图访问管理员接口被拦截", userId);
            throw new BusinessException(403, "无权访问，需要管理员/板主权限");
        }
    }
}
