package com.company.service.system.controller;

import com.company.common.core.ApiResult;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysConfig;
import com.company.service.system.service.SysConfigService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    @GetMapping("/page")
    public ApiResult<PageResult<SysConfig>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String configName,
                                                 @RequestParam(required = false) String configKey) {
        return ApiResult.ok(configService.page(pageNum, pageSize, configName, configKey));
    }

    @GetMapping("/value/{configKey}")
    public ApiResult<String> value(@PathVariable("configKey") String configKey) {
        return ApiResult.ok(configService.valueOf(configKey));
    }

    @PostMapping
    public ApiResult<SysConfig> create(@RequestBody SysConfig config) {
        return ApiResult.ok(configService.create(config));
    }

    @PutMapping
    public ApiResult<Void> update(@RequestBody SysConfig config) {
        configService.update(config);
        return ApiResult.ok(null);
    }

    @DeleteMapping("/{configIds}")
    public ApiResult<Void> delete(@PathVariable("configIds") Long[] configIds) {
        configService.delete(configIds);
        return ApiResult.ok(null);
    }
}
