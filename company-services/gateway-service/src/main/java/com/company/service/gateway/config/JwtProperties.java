package com.company.service.gateway.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 与 auth-service 的 company.auth.jwt 保持同一前缀和同一密钥，
 * 网关不签发令牌，只做离线校验，因此不需要 expire-minutes。
 */
@Data
@Component
@ConfigurationProperties(prefix = "company.auth.jwt")
public class JwtProperties {

    /** HS256 要求密钥不少于 32 字节，生产环境必须通过 JWT_SECRET 注入 */
    private String secret;
}
