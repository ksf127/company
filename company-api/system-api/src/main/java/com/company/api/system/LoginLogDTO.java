package com.company.api.system;

/**
 * 登录审计记录。status: 0 成功 / 1 失败。
 */
public record LoginLogDTO(
        String userName,
        String ipaddr,
        String browser,
        String os,
        String status,
        String msg) {
}
