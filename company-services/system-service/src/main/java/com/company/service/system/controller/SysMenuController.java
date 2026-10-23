package com.company.service.system.controller;

import com.company.common.core.ApiResult;
import com.company.service.system.entity.SysMenu;
import com.company.service.system.service.SysMenuService;

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
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @GetMapping("/list")
    public ApiResult<List<SysMenu>> list() {
        return ApiResult.ok(menuService.list());
    }

    @GetMapping("/{menuId}")
    public ApiResult<SysMenu> get(@PathVariable("menuId") Long menuId) {
        return ApiResult.ok(menuService.get(menuId));
    }

    @PostMapping
    public ApiResult<SysMenu> create(@RequestBody SysMenu menu) {
        return ApiResult.ok(menuService.create(menu));
    }

    @PutMapping
    public ApiResult<Void> update(@RequestBody SysMenu menu) {
        menuService.update(menu);
        return ApiResult.ok(null);
    }

    @DeleteMapping("/{menuIds}")
    public ApiResult<Void> delete(@PathVariable("menuIds") Long[] menuIds) {
        menuService.delete(menuIds);
        return ApiResult.ok(null);
    }
}
