package com.company.starter.job;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定时任务线程池配置，所有服务共享同一套默认值。
 */
@ConfigurationProperties(prefix = "company.job")
public class CompanyJobProperties {

    /** 调度线程数；定时任务很多的服务可在 L2 配置里单独放大 */
    private int poolSize = 4;

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
