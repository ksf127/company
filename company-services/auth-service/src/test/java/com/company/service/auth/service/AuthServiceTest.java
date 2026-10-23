package com.company.service.auth.service;

import com.company.api.system.LoginLogDTO;
import com.company.api.system.SysRoleDTO;
import com.company.api.system.SysUserAuthDTO;
import com.company.api.system.SystemClient;
import com.company.common.core.ApiResult;
import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.auth.dto.LoginRequest;
import com.company.service.auth.dto.LoginResult;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录流程单测：SystemClient / PasswordEncoder / TokenService 全部 Mock，
 * 不依赖 MySQL、Redis、Nacos，也不发起任何 HTTP 调用。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0";

    @Mock
    private SystemClient systemClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnTokenAndRecordSuccessLog() {
        stubActiveUser("$2a$10$hash", "admin123");
        when(systemClient.listRoles(1L))
                .thenReturn(ApiResult.ok(List.of(new SysRoleDTO(1L, "超级管理员", "admin", "0"))));
        when(systemClient.listPermissions(1L)).thenReturn(ApiResult.ok(List.of("*:*:*")));
        when(tokenService.create(1L, "admin", List.of("admin"))).thenReturn("jwt-token");
        when(tokenService.expireSeconds()).thenReturn(7200L);
        when(systemClient.recordLoginLog(any(LoginLogDTO.class))).thenReturn(ApiResult.ok(null));

        LoginResult result = authService.login(new LoginRequest("admin", "admin123"), "127.0.0.1", USER_AGENT);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.expiresInSeconds()).isEqualTo(7200L);
        verify(systemClient).recordLoginLog(argThat(log -> "0".equals(log.status()) && "Chrome".equals(log.browser())));
    }

    @Test
    void loginShouldRejectWrongPassword() {
        stubActiveUser("$2a$10$hash", "wrong-password");
        when(systemClient.recordLoginLog(any(LoginLogDTO.class))).thenReturn(ApiResult.ok(null));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong-password"),
                "127.0.0.1", USER_AGENT))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> assertThat(((BizException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED))
                .hasMessageContaining("用户名或密码错误");

        verify(tokenService, never()).create(any(), any(), any());
        verify(systemClient).recordLoginLog(argThat(log -> "1".equals(log.status())));
    }

    @Test
    void loginShouldRejectDisabledAccount() {
        when(systemClient.getAuthByUserName("admin")).thenReturn(ApiResult.ok(
                new SysUserAuthDTO(1L, "admin", "管理员", "$2a$10$hash", 100L, "1")));
        when(passwordEncoder.matches("admin123", "$2a$10$hash")).thenReturn(true);
        when(systemClient.recordLoginLog(any(LoginLogDTO.class))).thenReturn(ApiResult.ok(null));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "admin123"),
                "127.0.0.1", USER_AGENT))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> assertThat(((BizException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(tokenService, never()).create(any(), any(), any());
    }

    private void stubActiveUser(String encodedPassword, String rawPassword) {
        when(systemClient.getAuthByUserName("admin")).thenReturn(ApiResult.ok(
                new SysUserAuthDTO(1L, "admin", "管理员", encodedPassword, 100L, "0")));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn("admin123".equals(rawPassword));
    }
}
