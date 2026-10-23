package com.company.starter.redis;

import java.time.Duration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 统一缓存入口：业务代码只依赖这个类，不直接注入 StringRedisTemplate。
 * 这样做的好处是 key 前缀、空值缓存策略、序列化方式只有一个出口，
 * 100 个服务不会各自长出一套不一致的缓存实现。
 */
public class RedisCacheService {

    private static final String NULL_SENTINEL = "__NULL__";

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final CompanyRedisProperties properties;

    public RedisCacheService(StringRedisTemplate redisTemplate,
                             ObjectMapper objectMapper,
                             CompanyRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        String raw = redisTemplate.opsForValue().get(realKey(key));
        if (raw == null) {
            return null;
        }
        if (NULL_SENTINEL.equals(raw)) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, typeReference);
        } catch (Exception e) {
            throw new IllegalStateException("缓存反序列化失败: " + key, e);
        }
    }

    public void put(String key, Object value, Duration ttl) {
        try {
            if (value == null) {
                if (properties.isCacheNull()) {
                    redisTemplate.opsForValue().set(realKey(key), NULL_SENTINEL, properties.getCacheNullTtl());
                }
                return;
            }
            String json = objectMapper.writeValueAsString(value);
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                redisTemplate.opsForValue().set(realKey(key), json);
            } else {
                redisTemplate.opsForValue().set(realKey(key), json, ttl);
            }
        } catch (Exception e) {
            throw new IllegalStateException("缓存序列化失败: " + key, e);
        }
    }

    public void evict(String key) {
        redisTemplate.delete(realKey(key));
    }

    private String realKey(String key) {
        String prefix = properties.getKeyPrefix();
        return prefix == null || prefix.isEmpty() ? key : prefix + key;
    }
}
