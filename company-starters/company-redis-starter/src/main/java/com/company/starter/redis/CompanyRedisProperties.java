package com.company.starter.redis;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对应 Nacos L1 中的 company.redis.*，业务服务不再自己拼 key 前缀。
 */
@ConfigurationProperties(prefix = "company.redis")
public class CompanyRedisProperties {

    private String keyPrefix = "biz:";

    private boolean cacheNull = false;

    private Duration cacheNullTtl = Duration.ofSeconds(60);

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public boolean isCacheNull() {
        return cacheNull;
    }

    public void setCacheNull(boolean cacheNull) {
        this.cacheNull = cacheNull;
    }

    public Duration getCacheNullTtl() {
        return cacheNullTtl;
    }

    public void setCacheNullTtl(Duration cacheNullTtl) {
        this.cacheNullTtl = cacheNullTtl;
    }
}
