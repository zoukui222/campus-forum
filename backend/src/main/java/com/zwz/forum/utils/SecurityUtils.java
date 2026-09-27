package com.zwz.forum.utils;

import com.zwz.forum.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @Author: ljx
 * @Date: 2025/11/21 15:18
 */
public class SecurityUtils {
    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                throw new BusinessException(401, "用户未登录");
            }
            // 在 JwtAuthenticationFilter 里，我们将 Principal 设置为了 Long 类型的 userId,所以这边直接获取就OK了
            Object principal = authentication.getPrincipal();
            return Long.valueOf(principal.toString());
        } catch (Exception e) {
            throw new BusinessException(401, "获取登录用户信息失败");
        }
    }
}
