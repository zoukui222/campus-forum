package com.zwz.forum.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * @Author: ljx
 * @Date: 2025/11/21 14:10
 */
@Component
public class JwtUtils {
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7天过期

    /** 签名密钥由配置注入，不再硬编码在代码中（生产环境通过环境变量 JWT_SECRET 覆盖） */
    @Value("${jwt.secret:CampusForumDevSecretChangeMe2025!!!}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 生成 Token：除 userId 外，把角色一并写入 claims，供鉴权过滤器还原权限
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // 把 userId 存进 token
                .claim("role", role)     // 角色：USER / MODERATOR / ADMIN
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析 Token
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null; // 解析失败或过期
        }
    }
}
