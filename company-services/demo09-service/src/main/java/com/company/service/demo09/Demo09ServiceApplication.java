package com.company.service.demo09;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 评价中心示例服务：一个服务 = 一个独立进程 + 一份 L2 配置 + 一个前端模块。
 */
@SpringBootApplication
public class Demo09ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Demo09ServiceApplication.class, args);
    }
}
