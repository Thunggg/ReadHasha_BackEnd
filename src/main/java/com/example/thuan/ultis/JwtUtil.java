package com.example.thuan.ultis;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "01234567890123456789012345678901"; // 32-byte key
    private static final long EXPIRATION_TIME = 10 * 60 * 1000; // 10 phút
    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 phút
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 ngày

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email) // Chỉ lưu email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Tạo Access Token với username làm subject
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username) // lưu username vào subject
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Tạo Refresh Token với username làm subject
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username) // lưu username vào subject
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7); // Bỏ "Bearer "
            }

            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // Chỉ lấy email từ subject
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token đã hết hạn!");
        } catch (Exception e) {
            throw new RuntimeException("Token không hợp lệ!");
        }
    }
}
