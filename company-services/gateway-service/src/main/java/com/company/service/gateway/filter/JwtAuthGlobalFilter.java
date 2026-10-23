package com.company.service.gateway.filter;

import com.company.service.gateway.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import reactor.core.publisher.Mono;

/**
 * 全局 JWT 校验：网关只做离线验签，不依赖 auth-service，也不保存会话。
 * 校验通过后把用户身份注入 X-User-Id / X-Username，业务服务无需重复解析令牌。
 */
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String HEADER_USER_ID = "X-User-Id";

    private static final String HEADER_USERNAME = "X-Username";

    /** 放行清单：登录接口本身不需要令牌，健康检查/指标由运维直接访问 */
    private static final List<String> WHITELIST_PATTERNS = List.of("/api/auth/login", "/actuator/**");

    private static final String UNAUTHORIZED_BODY =
            "{\"code\":401,\"message\":\"未认证或令牌已失效\",\"data\":null}";

    private final SecretKey key;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthGlobalFilter(JwtProperties properties) {
        if (!StringUtils.hasText(properties.getSecret())
                || properties.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("company.auth.jwt.secret 未配置或长度不足 32 字节（HS256 要求）");
        }
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (HttpMethod.OPTIONS.equals(request.getMethod()) || isWhitelisted(request.getURI().getPath())) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange);
        }

        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authorization.substring(BEARER_PREFIX.length()))
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return unauthorized(exchange);
        }

        Object rawUserId = claims.get("userId");
        if (!(rawUserId instanceof Number userId)) {
            return unauthorized(exchange);
        }
        String username = claims.getSubject();

        // 先删除客户端伪造的同名头，再写入可信身份，避免越权
        ServerHttpRequest mutated = request.mutate()
                .headers(headers -> {
                    headers.remove(HEADER_USER_ID);
                    headers.remove(HEADER_USERNAME);
                    headers.set(HEADER_USER_ID, String.valueOf(userId.longValue()));
                    if (StringUtils.hasText(username)) {
                        headers.set(HEADER_USERNAME,
                                URLEncoder.encode(username, StandardCharsets.UTF_8));
                    }
                })
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory()
                .wrap(UNAUTHORIZED_BODY.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 在路由转发之前执行，业务服务收到的请求一定已通过鉴权
        return -100;
    }
}
