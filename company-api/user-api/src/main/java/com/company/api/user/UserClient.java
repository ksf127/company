package com.company.api.user;

import com.company.common.core.ApiResult;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 消费方引入 user-api 后，只需 @EnableFeignClients(basePackages = "com.company.api")。
 * URL 前缀交给网关/注册中心，契约里不出现 IP、端口、集群地址。
 */
@FeignClient(name = "user-service", path = "/internal/users")
public interface UserClient {

    @GetMapping("/{id}")
    ApiResult<UserDTO> getById(@PathVariable("id") Long id);
}
