package com.company.service.system.controller;

import com.company.common.core.ApiResult;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysLoginLog;
import com.company.service.system.service.SysLoginLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 登录日志只读：写入走 /internal/system/login-logs，由 auth-service 调用 */
@RestController
@RequestMapping("/system/log/login")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogService loginLogService;

    @GetMapping("/page")
    public ApiResult<PageResult<SysLoginLog>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false) String userName,
                                                   @RequestParam(required = false) String status) {
        return ApiResult.ok(loginLogService.page(pageNum, pageSize, userName, status));
    }
}
