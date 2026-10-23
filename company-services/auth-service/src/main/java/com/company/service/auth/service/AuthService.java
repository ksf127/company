package com.company.service.auth.service;

import com.company.api.system.LoginLogDTO;
import com.company.api.system.SysRoleDTO;
import com.company.api.system.SysUserAuthDTO;
import com.company.api.system.SysUserDTO;
import com.company.api.system.SystemClient;
import com.company.common.core.ApiResult;
import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.auth.dto.LoginRequest;
import com.company.service.auth.dto.LoginResult;
import com.company.service.auth.dto.UserInfoResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SystemClient systemClient;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    public LoginResult login(LoginRequest request, String ipaddr, String userAgent) {
        SysUserAuthDTO auth = unwrap(systemClient.getAuthByUserName(request.username()));
        if (auth == null || !passwordEncoder.matches(request.password(), auth.password())) {
            recordLog(request.username(), ipaddr, userAgent, "1", "用户名或密码错误");
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!"0".equals(auth.status())) {
            recordLog(request.username(), ipaddr, userAgent, "1", "账号已停用");
            throw new BizException(ErrorCode.FORBIDDEN, "账号已停用，请联系管理员");
        }

        List<SysRoleDTO> roles = unwrap(systemClient.listRoles(auth.userId()));
        List<String> roleKeys = roles == null ? List.of() : roles.stream().map(SysRoleDTO::roleKey).toList();
        List<String> permissions = unwrap(systemClient.listPermissions(auth.userId()));

        String token = tokenService.create(auth.userId(), auth.userName(), roleKeys);
        recordLog(request.username(), ipaddr, userAgent, "0", "登录成功");
        return new LoginResult(token, tokenService.expireSeconds());
    }

    public UserInfoResult getInfo(Long userId) {
        SysUserDTO user = unwrap(systemClient.getUser(userId));
        List<SysRoleDTO> roles = unwrap(systemClient.listRoles(userId));
        List<String> permissions = unwrap(systemClient.listPermissions(userId));
        return new UserInfoResult(user, roles == null ? List.of() : roles,
                permissions == null ? List.of() : permissions);
    }

    private <T> T unwrap(ApiResult<T> result) {
        if (result == null) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "system-service 返回为空");
        }
        if (!result.isSuccess()) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, result.message());
        }
        return result.data();
    }

    /** 审计失败不得影响登录主流程 */
    private void recordLog(String userName, String ipaddr, String userAgent, String status, String msg) {
        try {
            systemClient.recordLoginLog(new LoginLogDTO(userName, ipaddr,
                    browser(userAgent), os(userAgent), status, msg));
        } catch (RuntimeException ex) {
            log.warn("写入登录日志失败: {}", ex.getMessage());
        }
    }

    private static String browser(String ua) {
        if (!StringUtils.hasText(ua)) {
            return "Unknown";
        }
        if (ua.contains("Edg/")) {
            return "Edge";
        }
        if (ua.contains("Firefox/")) {
            return "Firefox";
        }
        if (ua.contains("Chrome/")) {
            return "Chrome";
        }
        if (ua.contains("Safari/")) {
            return "Safari";
        }
        return "Other";
    }

    private static String os(String ua) {
        if (!StringUtils.hasText(ua)) {
            return "Unknown";
        }
        if (ua.contains("Windows")) {
            return "Windows";
        }
        if (ua.contains("Android")) {
            return "Android";
        }
        if (ua.contains("iPhone") || ua.contains("iPad")) {
            return "iOS";
        }
        if (ua.contains("Mac OS X")) {
            return "macOS";
        }
        if (ua.contains("Linux")) {
            return "Linux";
        }
        return "Other";
    }
}
