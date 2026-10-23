package com.company.service.demo10.dto;

import java.util.List;

public record PageResult<T>(long total, List<T> rows) {
}
