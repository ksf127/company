package com.company.service.demo06.dto;

import java.util.List;

public record PageResult<T>(long total, List<T> rows) {
}
