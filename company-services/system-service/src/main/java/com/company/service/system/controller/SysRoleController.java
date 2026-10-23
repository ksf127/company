package com.company.service.system.controller;

import com.company.common.core.ApiResult;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysRole;
import com.company.service.system.service.SysRoleService;

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
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping("/page")
    public ApiResult<PageResult<SysRole>> page(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false) String roleName,
                                               @RequestParam(required = false) String status) {
        return ApiResult.ok(roleService.page(pageNum, pageSize, roleName, status));
    }

    @GetMapping("/list")
    public ApiResult<List<SysRole>> list() {
        return ApiResult.ok(roleService.listAll());
    }

    @GetMapping("/{roleId}")
    public ApiResult<SysRole> get(@PathVariable("roleId") Long roleId) {
        return ApiResult.ok(roleService.get(roleId));
    }

    @PostMapping
    public ApiResult<SysRole> create(@RequestBody SysRole role) {
        return ApiResult.ok(roleService.create(role));
    }

    @PutMapping
    public ApiResult<Void> update(@RequestBody SysRole role) {
        roleService.update(role);
        return ApiResult.ok(null);
    }

    @DeleteMapping("/{roleIds}")
    public ApiResult<Void> delete(@PathVariable("roleIds") Long[] roleIds) {
        roleService.delete(roleIds);
        return ApiResult.ok(null);
    }
}
