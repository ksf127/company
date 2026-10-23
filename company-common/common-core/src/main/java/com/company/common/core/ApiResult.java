package com.company.common.core;

/**
 * 统一返回结构。只有 code == 0 表示成功。
 */
public record ApiResult<T>(int code, String message, T data) {

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(0, "OK", data);
    }

    public static <T> ApiResult<T> fail(ErrorCode errorCode, String message) {
        return new ApiResult<>(errorCode.code(), message == null ? errorCode.message() : message, null);
    }

    public boolean isSuccess() {
        return code == 0;
    }
}
