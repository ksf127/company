package com.company.service.demo07.dto;

import java.util.List;

public record PageResult<T>(long total, List<T> rows) {
}
