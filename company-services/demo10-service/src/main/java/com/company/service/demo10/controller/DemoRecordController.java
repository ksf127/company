package com.company.service.demo10.controller;

import com.company.common.core.ApiResult;
import com.company.service.demo10.dto.PageResult;
import com.company.service.demo10.dto.SaveRecordRequest;
import com.company.service.demo10.entity.DemoRecord;
import com.company.service.demo10.service.DemoRecordService;

import jakarta.validation.Valid;

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
@RequestMapping("/records")
public class DemoRecordController {

    private final DemoRecordService service;

    public DemoRecordController(DemoRecordService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResult<PageResult<DemoRecord>> page(
            @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResult.ok(service.page(keyword, pageNum, pageSize));
    }

    @PostMapping
    public ApiResult<DemoRecord> create(@Valid @RequestBody SaveRecordRequest request) {
        return ApiResult.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<DemoRecord> update(@PathVariable("id") Long id, @Valid @RequestBody SaveRecordRequest request) {
        return ApiResult.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResult.ok(null);
    }
}
