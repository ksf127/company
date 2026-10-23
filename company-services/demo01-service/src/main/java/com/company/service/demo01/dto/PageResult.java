package com.company.service.demo01.dto;

import java.util.List;

public record PageResult<T>(long total, List<T> rows) {
}
