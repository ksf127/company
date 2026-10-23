package com.company.service.system.controller;

import com.company.common.core.ApiResult;
import com.company.service.system.entity.SysDept;
import com.company.service.system.service.SysDeptService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @GetMapping("/list")
    public ApiResult<List<SysDept>> list() {
        return ApiResult.ok(deptService.list());
    }

    @GetMapping("/{deptId}")
    public ApiResult<SysDept> get(@PathVariable("deptId") Long deptId) {
        return ApiResult.ok(deptService.get(deptId));
    }

    @PostMapping
    public ApiResult<SysDept> create(@RequestBody SysDept dept) {
        return ApiResult.ok(deptService.create(dept));
    }

    @PutMapping
    public ApiResult<Void> update(@RequestBody SysDept dept) {
        deptService.update(dept);
        return ApiResult.ok(null);
    }

    @DeleteMapping("/{deptIds}")
    public ApiResult<Void> delete(@PathVariable("deptIds") Long[] deptIds) {
        deptService.delete(deptIds);
        return ApiResult.ok(null);
    }
}
