package com.company.service.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 契约扫描范围固定为 com.company.api，服务自己的包不允许出现 FeignClient 定义。
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.company.api")
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
