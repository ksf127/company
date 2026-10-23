package com.company.service.auth.service;

import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.auth.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * 无状态 JWT：服务端不保存会话，网关/业务服务用同一密钥离线校验。
 */
@Service
public class TokenService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProperties properties;

    private final SecretKey key;

    public TokenService(JwtProperties properties) {
        this.properties = properties;
        if (!StringUtils.hasText(properties.getSecret())
                || properties.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("company.auth.jwt.secret 未配置或长度不足 32 字节（HS256 要求）");
        }
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String create(Long userId, String userName, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(properties.getExpireMinutes()));
        return Jwts.builder()
                .subject(userName)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "登录状态无效或已过期");
        }
    }

    /** 从 Authorization 头解析用户 ID，供 /auth/getInfo 使用 */
    public Long userIdFromHeader(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "缺少 Authorization 头");
        }
        Claims claims = parse(authorization.substring(BEARER_PREFIX.length()));
        Number userId = claims.get("userId", Number.class);
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "令牌缺少 userId");
        }
        return userId.longValue();
    }

    public long expireSeconds() {
        return Duration.ofMinutes(properties.getExpireMinutes()).toSeconds();
    }
}
