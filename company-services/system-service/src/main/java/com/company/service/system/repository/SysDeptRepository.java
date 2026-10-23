package com.company.service.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.service.system.entity.SysDept;
import com.company.service.system.mapper.SysDeptMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SysDeptRepository {

    private final SysDeptMapper deptMapper;

    public List<SysDept> findAll() {
        return deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getParentId)
                .orderByAsc(SysDept::getOrderNum));
    }

    public SysDept findById(Long deptId) {
        return deptMapper.selectById(deptId);
    }

    public void insert(SysDept dept) {
        deptMapper.insert(dept);
    }

    public void update(SysDept dept) {
        deptMapper.updateById(dept);
    }

    public void deleteByIds(Long[] deptIds) {
        deptMapper.deleteBatchIds(Arrays.asList(deptIds));
    }
}
