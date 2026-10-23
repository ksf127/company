package com.company.service.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysRole;
import com.company.service.system.mapper.SysRoleMapper;
import com.company.service.system.mapper.SysRoleMenuMapper;
import com.company.service.system.mapper.SysUserRoleMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SysRoleRepository {

    private final SysRoleMapper roleMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    private final SysUserRoleMapper userRoleMapper;

    public PageResult<SysRole> page(int pageNum, int pageSize, String roleName, String status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .eq(StringUtils.hasText(status), SysRole::getStatus, status)
                .orderByAsc(SysRole::getRoleSort);
        Page<SysRole> page = roleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public List<SysRole> findAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getRoleSort));
    }

    public SysRole findById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    public void insert(SysRole role) {
        roleMapper.insert(role);
    }

    public void update(SysRole role) {
        roleMapper.updateById(role);
    }

    public void deleteByIds(Long[] roleIds) {
        roleMapper.deleteBatchIds(Arrays.asList(roleIds));
        for (Long roleId : roleIds) {
            roleMenuMapper.deleteByRoleId(roleId);
            userRoleMapper.deleteByRoleId(roleId);
        }
    }
}
