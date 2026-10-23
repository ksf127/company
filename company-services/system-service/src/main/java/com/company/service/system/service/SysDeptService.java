package com.company.service.system.service;

import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.system.entity.SysDept;
import com.company.service.system.repository.SysDeptRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDeptService {

    private final SysDeptRepository deptRepository;

    public List<SysDept> list() {
        return deptRepository.findAll();
    }

    public SysDept get(Long deptId) {
        SysDept dept = deptRepository.findById(deptId);
        if (dept == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "部门不存在: " + deptId);
        }
        return dept;
    }

    @Transactional
    public SysDept create(SysDept dept) {
        dept.setDeptId(null);
        dept.setParentId(dept.getParentId() == null ? 0L : dept.getParentId());
        dept.setStatus(StringUtils.hasText(dept.getStatus()) ? dept.getStatus() : "0");
        dept.setDeleted(0);
        dept.setCreateTime(LocalDateTime.now());
        deptRepository.insert(dept);
        return dept;
    }

    @Transactional
    public void update(SysDept dept) {
        get(dept.getDeptId());
        if (dept.getDeptId().equals(dept.getParentId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "上级部门不能是自己");
        }
        deptRepository.update(dept);
    }

    @Transactional
    public void delete(Long[] deptIds) {
        if (deptIds == null || deptIds.length == 0) {
            return;
        }
        deptRepository.deleteByIds(deptIds);
    }
}
