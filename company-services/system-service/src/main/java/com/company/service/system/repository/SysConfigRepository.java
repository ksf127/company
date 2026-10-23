package com.company.service.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysConfig;
import com.company.service.system.mapper.SysConfigMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Repository
@RequiredArgsConstructor
public class SysConfigRepository {

    private final SysConfigMapper configMapper;

    public PageResult<SysConfig> page(int pageNum, int pageSize, String configName, String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .like(StringUtils.hasText(configName), SysConfig::getConfigName, configName)
                .like(StringUtils.hasText(configKey), SysConfig::getConfigKey, configKey)
                .orderByAsc(SysConfig::getConfigId);
        Page<SysConfig> page = configMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public SysConfig findById(Long configId) {
        return configMapper.selectById(configId);
    }

    public SysConfig findByKey(String configKey) {
        return configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .last("LIMIT 1"));
    }

    public void insert(SysConfig config) {
        configMapper.insert(config);
    }

    public void update(SysConfig config) {
        configMapper.updateById(config);
    }

    public void deleteByIds(Long[] configIds) {
        configMapper.deleteBatchIds(Arrays.asList(configIds));
    }
}
