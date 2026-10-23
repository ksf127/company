package com.company.service.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 统一入口：所有外部流量先到网关，鉴权、限流、路由都在这一层完成。
 * 网关本身无状态，可水平扩展；业务服务不再直接对外暴露。
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
