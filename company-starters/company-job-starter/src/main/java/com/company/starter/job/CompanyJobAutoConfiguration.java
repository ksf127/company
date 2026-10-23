package com.company.starter.job;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 引入 Starter 即开启调度能力：@Scheduled 方法由统一线程池执行，
 * 避免每个服务各自 new 线程池导致线程数失控。
 */
@AutoConfiguration
@ConditionalOnClass(ThreadPoolTaskScheduler.class)
@EnableScheduling
@EnableConfigurationProperties(CompanyJobProperties.class)
public class CompanyJobAutoConfiguration {

    @Bean(name = "taskScheduler")
    @ConditionalOnMissingBean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler(CompanyJobProperties properties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getPoolSize());
        scheduler.setThreadNamePrefix("company-job-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setErrorHandler(throwable ->
                org.slf4j.LoggerFactory.getLogger(CompanyJobAutoConfiguration.class)
                        .error("定时任务执行失败", throwable));
        return scheduler;
    }
}
