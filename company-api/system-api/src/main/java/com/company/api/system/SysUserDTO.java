package com.company.api.system;

/**
 * 用户基础信息（不含密码）。跨服务传输只允许出现不可变 DTO。
 */
public record SysUserDTO(
        Long userId,
        String userName,
        String nickName,
        String email,
        String phonenumber,
        Long deptId,
        String status) {
}
