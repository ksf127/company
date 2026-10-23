package com.company.common.web;

import com.company.common.core.ApiResult;
import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常出口：业务异常按错误码返回，未知异常只回 500，不向调用方泄漏堆栈。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ApiResult<Void> handleBizException(BizException e) {
        log.warn("biz exception: code={}, message={}", e.getErrorCode().code(), e.getMessage());
        return ApiResult.fail(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? ErrorCode.PARAM_INVALID.message()
                : fieldError.getField() + " " + fieldError.getDefaultMessage();
        return ApiResult.fail(ErrorCode.PARAM_INVALID, message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleUnknown(Exception e) {
        log.error("unhandled exception", e);
        return ApiResult.fail(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.message());
    }
}
