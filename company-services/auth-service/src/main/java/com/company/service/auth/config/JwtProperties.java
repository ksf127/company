package com.company.service.auth.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "company.auth.jwt")
public class JwtProperties {

    /** HS256 要求密钥不少于 32 字节，生产环境必须通过 JWT_SECRET 注入 */
    private String secret;

    private long expireMinutes = 120;
}
