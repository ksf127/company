package com.company.service.order.controller;

import com.company.common.core.ApiResult;
import com.company.service.order.dto.CreateOrderRequest;
import com.company.service.order.dto.OrderView;
import com.company.service.order.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResult<OrderView> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResult.ok(orderService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResult<OrderView> get(@PathVariable("id") Long id) {
        return ApiResult.ok(orderService.findById(id));
    }
}
