package com.company.service.system.service;

import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysConfig;
import com.company.service.system.repository.SysConfigRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigRepository configRepository;

    public PageResult<SysConfig> page(int pageNum, int pageSize, String configName, String configKey) {
        return configRepository.page(pageNum, pageSize, configName, configKey);
    }

    public String valueOf(String configKey) {
        SysConfig config = configRepository.findByKey(configKey);
        return config == null ? null : config.getConfigValue();
    }

    @Transactional
    public SysConfig create(SysConfig config) {
        if (configRepository.findByKey(config.getConfigKey()) != null) {
            throw new BizException(ErrorCode.CONFLICT, "参数键名已存在: " + config.getConfigKey());
        }
        config.setConfigId(null);
        config.setConfigType("N");
        config.setCreateTime(LocalDateTime.now());
        configRepository.insert(config);
        return config;
    }

    @Transactional
    public void update(SysConfig config) {
        if (configRepository.findById(config.getConfigId()) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "参数不存在: " + config.getConfigId());
        }
        configRepository.update(config);
    }

    @Transactional
    public void delete(Long[] configIds) {
        if (configIds == null || configIds.length == 0) {
            return;
        }
        for (Long configId : configIds) {
            SysConfig config = configRepository.findById(configId);
            if (config != null && "Y".equals(config.getConfigType())) {
                throw new BizException(ErrorCode.FORBIDDEN, "系统内置参数不允许删除: " + config.getConfigKey());
            }
        }
        configRepository.deleteByIds(configIds);
    }
}
