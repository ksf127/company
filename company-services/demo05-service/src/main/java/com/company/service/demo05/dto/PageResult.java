package com.company.service.demo05.dto;

import java.util.List;

public record PageResult<T>(long total, List<T> rows) {
}
