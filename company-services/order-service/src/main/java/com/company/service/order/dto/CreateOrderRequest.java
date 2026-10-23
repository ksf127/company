package com.company.service.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotNull(message = "userId 不能为空") Long userId,
        @NotNull(message = "amount 不能为空")
        @DecimalMin(value = "0.01", message = "amount 必须大于 0") BigDecimal amount) {
}
