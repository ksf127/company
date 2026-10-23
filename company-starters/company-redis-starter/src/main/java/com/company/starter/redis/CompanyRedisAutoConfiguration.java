package com.company.starter.redis;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 引入 company-redis-starter 即自动生效，业务服务不需要写 @Import 或 @ComponentScan。
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@EnableConfigurationProperties(CompanyRedisProperties.class)
@ConditionalOnClass(StringRedisTemplate.class)
public class CompanyRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisCacheService redisCacheService(StringRedisTemplate stringRedisTemplate,
                                               ObjectMapper objectMapper,
                                               CompanyRedisProperties properties) {
        return new RedisCacheService(stringRedisTemplate, objectMapper, properties);
    }
}
