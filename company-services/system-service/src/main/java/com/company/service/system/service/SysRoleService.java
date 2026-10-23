package com.company.service.system.service;

import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysRole;
import com.company.service.system.repository.SysRoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleRepository roleRepository;

    public PageResult<SysRole> page(int pageNum, int pageSize, String roleName, String status) {
        return roleRepository.page(pageNum, pageSize, roleName, status);
    }

    public List<SysRole> listAll() {
        return roleRepository.findAll();
    }

    public SysRole get(Long roleId) {
        SysRole role = roleRepository.findById(roleId);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在: " + roleId);
        }
        return role;
    }

    @Transactional
    public SysRole create(SysRole role) {
        role.setRoleId(null);
        role.setStatus(StringUtils.hasText(role.getStatus()) ? role.getStatus() : "0");
        role.setDeleted(0);
        role.setCreateTime(LocalDateTime.now());
        roleRepository.insert(role);
        return role;
    }

    @Transactional
    public void update(SysRole role) {
        get(role.getRoleId());
        roleRepository.update(role);
    }

    @Transactional
    public void delete(Long[] roleIds) {
        if (roleIds == null || roleIds.length == 0) {
            return;
        }
        roleRepository.deleteByIds(roleIds);
    }
}
