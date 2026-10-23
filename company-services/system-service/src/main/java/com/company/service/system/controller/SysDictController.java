package com.company.service.system.controller;

import com.company.common.core.ApiResult;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysDictData;
import com.company.service.system.entity.SysDictType;
import com.company.service.system.service.SysDictService;

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
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService dictService;

    @GetMapping("/type/page")
    public ApiResult<PageResult<SysDictType>> pageTypes(@RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize,
                                                        @RequestParam(required = false) String dictName,
                                                        @RequestParam(required = false) String dictType) {
        return ApiResult.ok(dictService.pageTypes(pageNum, pageSize, dictName, dictType));
    }

    @GetMapping("/type/list")
    public ApiResult<List<SysDictType>> listTypes() {
        return ApiResult.ok(dictService.listTypes());
    }

    @PostMapping("/type")
    public ApiResult<SysDictType> createType(@RequestBody SysDictType type) {
        return ApiResult.ok(dictService.createType(type));
    }

    @PutMapping("/type")
    public ApiResult<Void> updateType(@RequestBody SysDictType type) {
        dictService.updateType(type);
        return ApiResult.ok(null);
    }

    @DeleteMapping("/type/{dictIds}")
    public ApiResult<Void> deleteTypes(@PathVariable("dictIds") Long[] dictIds) {
        dictService.deleteTypes(dictIds);
        return ApiResult.ok(null);
    }

    @GetMapping("/data/page")
    public ApiResult<PageResult<SysDictData>> pageData(@RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "10") int pageSize,
                                                       @RequestParam String dictType) {
        return ApiResult.ok(dictService.pageData(pageNum, pageSize, dictType));
    }

    /** 前端下拉框使用：按字典类型取启用数据 */
    @GetMapping("/data/type/{dictType}")
    public ApiResult<List<SysDictData>> dataByType(@PathVariable("dictType") String dictType) {
        return ApiResult.ok(dictService.dataByType(dictType));
    }

    @PostMapping("/data")
    public ApiResult<SysDictData> createData(@RequestBody SysDictData data) {
        return ApiResult.ok(dictService.createData(data));
    }

    @PutMapping("/data")
    public ApiResult<Void> updateData(@RequestBody SysDictData data) {
        dictService.updateData(data);
        return ApiResult.ok(null);
    }

    @DeleteMapping("/data/{dictCodes}")
    public ApiResult<Void> deleteData(@PathVariable("dictCodes") Long[] dictCodes) {
        dictService.deleteData(dictCodes);
        return ApiResult.ok(null);
    }
}
