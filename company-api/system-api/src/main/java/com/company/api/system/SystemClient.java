package com.company.api.system;

import com.company.common.core.ApiResult;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * system-service 的内部契约。auth-service 等调用方只需引入 system-api。
 * 所有返回值统一包 ApiResult，避免"服务端包了一层、客户端却按裸对象反序列化"的静默 null。
 */
@FeignClient(name = "system-service", path = "/internal/system")
public interface SystemClient {

    @GetMapping("/users/by-username/{userName}")
    ApiResult<SysUserAuthDTO> getAuthByUserName(@PathVariable("userName") String userName);

    @GetMapping("/users/{userId}")
    ApiResult<SysUserDTO> getUser(@PathVariable("userId") Long userId);

    @GetMapping("/users/{userId}/roles")
    ApiResult<List<SysRoleDTO>> listRoles(@PathVariable("userId") Long userId);

    @GetMapping("/users/{userId}/permissions")
    ApiResult<List<String>> listPermissions(@PathVariable("userId") Long userId);

    @PostMapping("/login-logs")
    ApiResult<Void> recordLoginLog(@RequestBody LoginLogDTO log);
}
