package com.company.service.system.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 示例定时任务：只做运行态汇总日志，不写库、不调用外部服务。
 * 调度线程由 company-job-starter 提供的 ThreadPoolTaskScheduler 承载。
 */
@Component
public class SystemSummaryJob {

    private static final Logger log = LoggerFactory.getLogger(SystemSummaryJob.class);

    private final AtomicLong rounds = new AtomicLong();

    @Scheduled(initialDelayString = "${company.job.summary-initial-delay-ms:30000}",
            fixedDelayString = "${company.job.summary-interval-ms:60000}")
    public void summarize() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long round = rounds.incrementAndGet();
        log.info("system-service 定时汇总: round={}, heapUsed={}MB, heapMax={}MB, threads={}",
                round,
                heap.getUsed() / (1024 * 1024),
                heap.getMax() > 0 ? heap.getMax() / (1024 * 1024) : -1,
                ManagementFactory.getThreadMXBean().getThreadCount());
    }
}
