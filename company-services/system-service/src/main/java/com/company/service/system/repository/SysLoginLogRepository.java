package com.company.service.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysLoginLog;
import com.company.service.system.mapper.SysLoginLogMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class SysLoginLogRepository {

    private final SysLoginLogMapper logMapper;

    public PageResult<SysLoginLog> page(int pageNum, int pageSize, String userName, String status) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<SysLoginLog>()
                .like(StringUtils.hasText(userName), SysLoginLog::getUserName, userName)
                .eq(StringUtils.hasText(status), SysLoginLog::getStatus, status)
                .orderByDesc(SysLoginLog::getLoginTime);
        Page<SysLoginLog> page = logMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public void insert(SysLoginLog log) {
        logMapper.insert(log);
    }
}
