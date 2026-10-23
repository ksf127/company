package com.company.service.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderView(
        Long id,
        Long userId,
        String userName,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt) {
}
