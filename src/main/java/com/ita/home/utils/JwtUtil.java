package com.ita.home.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT令牌
 * 
 * @author Mikkeyf
 * @since 2025-09-22
 */
@Slf4j
@Component
@Data
public class JwtUtil {

    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * token过期时间（小时）
     */
    @Value("${jwt.expire-hours}")
    private int expireHours;

    /**
     * token前缀
     */
    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    /**
     * header名称
     * -- GETTER --
     *  获取header名称

     */
    @Getter
    @Value("${jwt.header-name}")
    private String headerName;

    /**
     * 生成JWT密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT令牌字符串
     */
    public String generateToken(Long userId, String username) {
        try {
            // 设置载荷信息
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("username", username);
            claims.put("timestamp", System.currentTimeMillis());

            // 设置过期时间
            Date expirationDate = new Date(System.currentTimeMillis() + (long) expireHours * 60 * 60 * 1000);

            // 生成JWT
            String token = Jwts.builder()
                    .setClaims(claims)                    // 设置载荷
                    .setSubject(username)                 // 设置主题（通常是用户名）
                    .setIssuedAt(new Date())             // 设置签发时间
                    .setExpiration(expirationDate)       // 设置过期时间
                    .signWith(getSigningKey())           // 设置签名密钥
                    .compact();

            log.info("为用户 {} 生成JWT令牌成功，过期时间: {}", username, expirationDate);
            return token;

        } catch (Exception e) {
            log.error("生成JWT令牌失败，用户: {}", username, e);
            throw new RuntimeException("生成JWT令牌失败", e);
        }
    }

    /**
     * 从令牌中获取声明信息
     * 
     * @param token JWT令牌
     * @return Claims对象，包含令牌的载荷信息
     */
    public Claims getClaimsFromToken(String token) {
        try {
            // 移除token前缀
            if (token.startsWith(tokenPrefix)) {
                token = token.substring(tokenPrefix.length());
            }

            // 解析token
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
            throw new RuntimeException("令牌已过期，请重新登录");
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
            throw new RuntimeException("不支持的令牌格式");
        } catch (MalformedJwtException e) {
            log.error("JWT令牌格式错误: {}", e.getMessage());
            throw new RuntimeException("令牌格式错误");
        } catch (SecurityException e) {
            log.error("JWT令牌签名验证失败: {}", e.getMessage());
            throw new RuntimeException("令牌签名验证失败");
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌参数不合法: {}", e.getMessage());
            throw new RuntimeException("令牌参数不合法");
        }
    }

    /**
     * 从令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else {
            throw new RuntimeException("令牌中用户ID格式错误");
        }
    }

    /**
     * 从令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 检查令牌是否过期
     * 
     * @param token JWT令牌
     * @return true=已过期，false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("检查令牌过期状态时发生错误", e);
            return true; // 出现异常时认为已过期
        }
    }

    /**
     * 验证JWT令牌是否有效
     * 
     * @param token JWT令牌
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String username = claims.getSubject();
            Long userId = getUserIdFromToken(token);
            
            // 检查必要字段是否存在
            if (username == null || username.trim().isEmpty() || userId == null || userId <= 0) {
                log.error("JWT令牌缺少必要字段: username={}, userId={}", username, userId);
                return false;
            }

            // 检查是否过期
            if (isTokenExpired(token)) {
                log.warn("JWT令牌已过期，用户: {}", username);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("验证JWT令牌时发生错误", e);
            return false;
        }
    }

    /**
     * 从HTTP请求头中提取JWT令牌
     * 
     * @param authHeader Authorization请求头的值
     * @return JWT令牌字符串，如果格式不正确则返回null
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(tokenPrefix)) {
            return authHeader.substring(tokenPrefix.length());
        }
        return null;
    }
}