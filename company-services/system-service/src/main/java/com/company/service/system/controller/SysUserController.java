package com.company.service.system.controller;

import com.company.common.core.ApiResult;
import com.company.service.system.dto.PageResult;
import com.company.service.system.dto.SysUserSaveRequest;
import com.company.service.system.entity.SysUser;
import com.company.service.system.service.SysUserService;

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

import java.util.List;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/page")
    public ApiResult<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false) String userName,
                                               @RequestParam(required = false) String status) {
        return ApiResult.ok(userService.page(pageNum, pageSize, userName, status));
    }

    @GetMapping("/{userId}")
    public ApiResult<SysUser> get(@PathVariable("userId") Long userId) {
        return ApiResult.ok(userService.get(userId));
    }

    @GetMapping("/{userId}/roleIds")
    public ApiResult<List<Long>> roleIds(@PathVariable("userId") Long userId) {
        return ApiResult.ok(userService.roleIds(userId));
    }

    @PostMapping
    public ApiResult<SysUser> create(@RequestBody SysUserSaveRequest request) {
        return ApiResult.ok(userService.create(request.toEntity(), request.roleIds()));
    }

    @PutMapping
    public ApiResult<Void> update(@RequestBody SysUserSaveRequest request) {
        userService.update(request.toEntity(), request.roleIds());
        return ApiResult.ok(null);
    }

    @DeleteMapping("/{userIds}")
    public ApiResult<Void> delete(@PathVariable("userIds") Long[] userIds) {
        userService.delete(userIds);
        return ApiResult.ok(null);
    }
}
