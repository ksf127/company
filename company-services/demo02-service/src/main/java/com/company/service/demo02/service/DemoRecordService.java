package com.company.service.demo02.service;

import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.demo02.dto.PageResult;
import com.company.service.demo02.dto.SaveRecordRequest;
import com.company.service.demo02.entity.DemoRecord;
import com.company.service.demo02.repository.DemoRecordRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoRecordService {

    private final DemoRecordRepository repository;

    public DemoRecordService(DemoRecordRepository repository) {
        this.repository = repository;
    }

    public PageResult<DemoRecord> page(String keyword, int pageNum, int pageSize) {
        int safePage = Math.max(pageNum, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        List<DemoRecord> all = repository.findAll(keyword);
        int from = Math.min((safePage - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return new PageResult<>(all.size(), List.copyOf(all.subList(from, to)));
    }

    public DemoRecord create(SaveRecordRequest request) {
        return repository.save(request.name(), request.owner(), request.status(), request.remark());
    }

    public DemoRecord update(Long id, SaveRecordRequest request) {
        return repository.update(id, request.name(), request.owner(), request.status(), request.remark())
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "记录不存在: " + id));
    }

    public void delete(Long id) {
        if (!repository.delete(id)) {
            throw new BizException(ErrorCode.NOT_FOUND, "记录不存在: " + id);
        }
    }
}
