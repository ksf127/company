package com.company.service.system.service;

import com.company.api.system.SysUserAuthDTO;
import com.company.api.system.SysUserDTO;
import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysRole;
import com.company.service.system.entity.SysUser;
import com.company.service.system.repository.SysUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserService {

    /** 新建用户未指定密码时的初始密码，与若依默认一致，首次登录后应强制修改 */
    public static final String DEFAULT_PASSWORD = "123456";

    private final SysUserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public PageResult<SysUser> page(int pageNum, int pageSize, String userName, String status) {
        return userRepository.page(pageNum, pageSize, userName, status);
    }

    public SysUser get(Long userId) {
        SysUser user = userRepository.findById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在: " + userId);
        }
        return user;
    }

    public List<Long> roleIds(Long userId) {
        get(userId);
        return userRepository.findRoleIds(userId);
    }

    @Transactional
    public SysUser create(SysUser user, List<Long> roleIds) {
        if (userRepository.findByUserName(user.getUserName()) != null) {
            throw new BizException(ErrorCode.CONFLICT, "登录账号已存在: " + user.getUserName());
        }
        String rawPassword = StringUtils.hasText(user.getPassword()) ? user.getPassword() : DEFAULT_PASSWORD;
        user.setUserId(null);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setStatus(StringUtils.hasText(user.getStatus()) ? user.getStatus() : "0");
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setCreateBy("system");
        userRepository.insert(user);
        if (roleIds != null) {
            userRepository.replaceRoles(user.getUserId(), roleIds);
        }
        return user;
    }

    @Transactional
    public void update(SysUser user, List<Long> roleIds) {
        SysUser existing = get(user.getUserId());
        // 前端不传密码时表示"不修改"；传了空串也不覆盖
        user.setPassword(StringUtils.hasText(user.getPassword())
                ? passwordEncoder.encode(user.getPassword())
                : null);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy("system");
        userRepository.update(user);
        if (roleIds != null) {
            userRepository.replaceRoles(existing.getUserId(), roleIds);
        }
    }

    @Transactional
    public void delete(Long[] userIds) {
        if (userIds == null || userIds.length == 0) {
            return;
        }
        userRepository.deleteByIds(userIds);
        userRepository.deleteRoles(List.of(userIds));
    }

    public List<SysRole> roles(Long userId) {
        return userRepository.findRoles(userId);
    }

    public List<String> permissions(Long userId) {
        // 与若依一致：超级管理员拥有全部权限
        boolean admin = roles(userId).stream().anyMatch(role -> "admin".equals(role.getRoleKey()));
        return admin ? List.of("*:*:*") : userRepository.findPermissions(userId);
    }

    /** 认证入口：包含密码散列，只允许 /internal/** 调用 */
    public SysUserAuthDTO authByUserName(String userName) {
        SysUser user = userRepository.findByUserName(userName);
        if (user == null) {
            return null;
        }
        return new SysUserAuthDTO(user.getUserId(), user.getUserName(), user.getNickName(),
                user.getPassword(), user.getDeptId(), user.getStatus());
    }

    public SysUserDTO toDto(SysUser user) {
        return new SysUserDTO(user.getUserId(), user.getUserName(), user.getNickName(),
                user.getEmail(), user.getPhonenumber(), user.getDeptId(), user.getStatus());
    }
}
