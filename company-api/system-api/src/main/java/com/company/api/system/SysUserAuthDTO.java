package com.company.api.system;

/**
 * 认证专用用户信息，包含 BCrypt 密码散列。
 * 只允许在 /internal/** 上传输，网关必须拦截外部访问。
 */
public record SysUserAuthDTO(
        Long userId,
        String userName,
        String nickName,
        String password,
        Long deptId,
        String status) {
}
