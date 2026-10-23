package com.company.service.system.controller;

import com.company.api.system.LoginLogDTO;
import com.company.api.system.SysRoleDTO;
import com.company.api.system.SysUserAuthDTO;
import com.company.api.system.SysUserDTO;
import com.company.common.core.ApiResult;
import com.company.service.system.service.SysLoginLogService;
import com.company.service.system.service.SysUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * /internal/** 只暴露给集群内部调用，网关层会拦截外部访问。
 */
@RestController
@RequestMapping("/internal/system")
@RequiredArgsConstructor
public class InternalSystemController {

    private final SysUserService userService;

    private final SysLoginLogService loginLogService;

    @GetMapping("/users/by-username/{userName}")
    public ApiResult<SysUserAuthDTO> getAuthByUserName(@PathVariable("userName") String userName) {
        return ApiResult.ok(userService.authByUserName(userName));
    }

    @GetMapping("/users/{userId}")
    public ApiResult<SysUserDTO> getUser(@PathVariable("userId") Long userId) {
        return ApiResult.ok(userService.toDto(userService.get(userId)));
    }

    @GetMapping("/users/{userId}/roles")
    public ApiResult<List<SysRoleDTO>> listRoles(@PathVariable("userId") Long userId) {
        List<SysRoleDTO> roles = userService.roles(userId).stream()
                .map(role -> new SysRoleDTO(role.getRoleId(), role.getRoleName(), role.getRoleKey(), role.getStatus()))
                .toList();
        return ApiResult.ok(roles);
    }

    @GetMapping("/users/{userId}/permissions")
    public ApiResult<List<String>> listPermissions(@PathVariable("userId") Long userId) {
        return ApiResult.ok(userService.permissions(userId));
    }

    @PostMapping("/login-logs")
    public ApiResult<Void> recordLoginLog(@RequestBody LoginLogDTO log) {
        loginLogService.record(log.userName(), log.ipaddr(), log.browser(), log.os(), log.status(), log.msg());
        return ApiResult.ok(null);
    }
}
