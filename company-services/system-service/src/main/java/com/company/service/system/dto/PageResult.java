package com.company.service.system.dto;

import java.util.List;

/** 分页结果：字段名与若依前端保持一致（total + rows） */
public record PageResult<T>(long total, List<T> rows) {

    public static <T> PageResult<T> empty() {
        return new PageResult<>(0, List.of());
    }
}
