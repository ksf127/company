package com.company.service.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证中心：只负责校验凭证并签发 JWT，不保存会话，可水平扩展。
 * 契约扫描范围固定为 com.company.api。
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.company.api")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
