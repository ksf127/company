package com.company.service.order.service;

import com.company.api.user.UserClient;
import com.company.api.user.UserDTO;
import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.order.dto.CreateOrderRequest;
import com.company.service.order.dto.OrderView;
import com.company.starter.redis.RedisCacheService;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 示例业务：写库省略，重点展示"缓存 + 跨服务契约"的正确用法。
 * 业务代码只依赖 RedisCacheService 与 UserClient，看不到任何客户端细节。
 */
@Service
public class OrderService {

    private static final Duration ORDER_CACHE_TTL = Duration.ofMinutes(10);

    private static final TypeReference<OrderView> ORDER_VIEW_TYPE = new TypeReference<>() {
    };

    private final Map<Long, OrderView> store = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1000);

    private final RedisCacheService cache;

    private final UserClient userClient;

    public OrderService(RedisCacheService cache, UserClient userClient) {
        this.cache = cache;
        this.userClient = userClient;
    }

    public OrderView create(CreateOrderRequest request) {
        UserDTO user = userClient.getById(request.userId()).data();
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在: " + request.userId());
        }
        OrderView view = new OrderView(
                idGenerator.incrementAndGet(),
                user.id(),
                user.displayName(),
                request.amount(),
                "CREATED",
                LocalDateTime.now());
        store.put(view.id(), view);
        cache.put(cacheKey(view.id()), view, ORDER_CACHE_TTL);
        return view;
    }

    public OrderView findById(Long id) {
        OrderView cached = cache.get(cacheKey(id), ORDER_VIEW_TYPE);
        if (cached != null) {
            return cached;
        }
        OrderView view = store.get(id);
        if (view == null) {
            // 空值也走统一缓存策略，防止穿透
            cache.put(cacheKey(id), null, ORDER_CACHE_TTL);
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在: " + id);
        }
        cache.put(cacheKey(id), view, ORDER_CACHE_TTL);
        return view;
    }

    private String cacheKey(Long id) {
        return "order:" + id;
    }
}
