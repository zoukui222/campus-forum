package com.zwz.forum.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * @Author: ljx
 * @Date: 2025/11/21 14:10
 */
@Component
public class JwtUtils {
    private static final String SECRET = "CampusForumSecretKeyForJwtSigning2025!!!";
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7天过期

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 生成 Token
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // 把 userId 存进 token
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
