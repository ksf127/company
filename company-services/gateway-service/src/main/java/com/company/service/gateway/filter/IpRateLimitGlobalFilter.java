package com.company.service.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import reactor.core.publisher.Mono;

/**
 * 每 IP 每秒请求数限制（单机内存实现）。
 * 集群部署时应换成 Redis + RequestRateLimiter；这里刻意不引入新的中间件。
 */
@Component
public class IpRateLimitGlobalFilter implements GlobalFilter, Ordered {

    private static final int CLEANUP_THRESHOLD = 10_000;

    private static final String TOO_MANY_REQUESTS_BODY =
            "{\"code\":429,\"message\":\"请求过于频繁，请稍后重试\",\"data\":null}";

    private final int limitPerSecond;

    /** key = 客户端 IP，value = 当前秒窗口内的计数 */
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public IpRateLimitGlobalFilter(
            @Value("${company.gateway.rate-limit-per-second:200}") int limitPerSecond) {
        this.limitPerSecond = limitPerSecond;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long currentSecond = System.currentTimeMillis() / 1000;
        String clientIp = resolveClientIp(exchange.getRequest());
        Window window = windows.compute(clientIp, (ip, existing) -> existing == null || existing.second() != currentSecond
                ? new Window(currentSecond, 1)
                : new Window(currentSecond, existing.count() + 1));

        if (windows.size() > CLEANUP_THRESHOLD) {
            windows.entrySet().removeIf(entry -> entry.getValue().second() < currentSecond - 1);
        }

        if (window.count() > limitPerSecond) {
            return tooManyRequests(exchange);
        }
        return chain.filter(exchange);
    }

    private String resolveClientIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress == null || remoteAddress.getAddress() == null
                ? "unknown"
                : remoteAddress.getAddress().getHostAddress();
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory()
                .wrap(TOO_MANY_REQUESTS_BODY.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 最外层：先限流再鉴权，避免被刷令牌拖垮验签
        return -200;
    }

    private record Window(long second, int count) {
    }
}
