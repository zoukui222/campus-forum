package com.zwz.forum.security;

import com.zwz.forum.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * @Author: ljx
 * @Date: 2025/11/21 15:12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. 获取请求头中的 Token
        String header = request.getHeader("Authorization");

        // 2. 判断格式是否正确 (Bearer xxxx)
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response); // 没有 Token，直接放行(让 Security 后面的过滤器去拦截)
            return;
        }

        // 3. 解析 Token
        String token = header.substring(7); // 去掉 "Bearer "
        Claims claims = jwtUtils.parseToken(token);

        // 4. 验证成功，将用户信息放入 SecurityContext
        if (claims != null) {
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            // 角色取自 token，映射为 Spring Security 的 authority：ROLE_USER / ROLE_MODERATOR / ROLE_ADMIN
            String role = claims.get("role", String.class);
            if (!StringUtils.hasText(role)) {
                role = "USER"; // 兼容未携带角色的旧 token
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, // Principal (这里我们直接存 userId，方便后续获取)
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)) // Authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 继续过滤器链
        chain.doFilter(request, response);
    }
}