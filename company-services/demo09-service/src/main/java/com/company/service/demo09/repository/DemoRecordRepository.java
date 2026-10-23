package com.company.service.demo09.repository;

import com.company.service.demo09.entity.DemoRecord;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 演示用内存仓储：替换为 MyBatis-Plus 实现时，service 层无需改动。
 */
@Repository
public class DemoRecordRepository {

    private final Map<Long, DemoRecord> store = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1000);

    public DemoRecordRepository() {
        save("订单评价-9001", "admin", "0", "服务启动时写入的初始化数据");
        save("订单评价-9002", "ops", "1", "用于演示停用状态");
    }

    public List<DemoRecord> findAll(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();
        return store.values().stream()
                .filter(item -> normalized.isEmpty()
                        || item.name().toLowerCase().contains(normalized)
                        || item.owner().toLowerCase().contains(normalized))
                .sorted(Comparator.comparing(DemoRecord::id).reversed())
                .toList();
    }

    public Optional<DemoRecord> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public DemoRecord save(String name, String owner, String status, String remark) {
        long id = idGenerator.incrementAndGet();
        DemoRecord record = new DemoRecord(id, name, owner, status, remark, LocalDateTime.now());
        store.put(id, record);
        return record;
    }

    public Optional<DemoRecord> update(Long id, String name, String owner, String status, String remark) {
        return findById(id).map(existing -> {
            DemoRecord updated = new DemoRecord(id, name, owner, status, remark, existing.createdAt());
            store.put(id, updated);
            return updated;
        });
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }
}
