package com.company.service.auth.controller;

import com.company.common.core.ApiResult;
import com.company.service.auth.dto.LoginRequest;
import com.company.service.auth.dto.LoginResult;
import com.company.service.auth.dto.UserInfoResult;
import com.company.service.auth.service.AuthService;
import com.company.service.auth.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final TokenService tokenService;

    @PostMapping("/login")
    public ApiResult<LoginResult> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ApiResult.ok(authService.login(request, clientIp(servletRequest),
                servletRequest.getHeader("User-Agent")));
    }

    /** JWT 无状态，退出只需前端清除令牌；保留接口以便后续接入黑名单 */
    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        return ApiResult.ok(null);
    }

    @GetMapping("/getInfo")
    public ApiResult<UserInfoResult> getInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResult.ok(authService.getInfo(tokenService.userIdFromHeader(authorization)));
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
