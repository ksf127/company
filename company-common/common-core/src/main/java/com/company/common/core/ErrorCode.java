package com.company.common.core;

/**
 * 全局错误码。各业务线在 10000 以上分段扩展，禁止复用同一区段。
 */
public enum ErrorCode {

    SUCCESS(0, "OK"),
    PARAM_INVALID(400, "参数不合法"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    SYSTEM_ERROR(500, "系统繁忙");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
