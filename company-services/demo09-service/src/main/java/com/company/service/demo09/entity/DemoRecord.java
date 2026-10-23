package com.company.service.demo09.entity;

import java.time.LocalDateTime;

/**
 * 演示模型：真实服务应替换为数据库实体，并把存储实现收敛到 repository 层。
 */
public record DemoRecord(Long id, String name, String owner, String status, String remark, LocalDateTime createdAt) {
}
