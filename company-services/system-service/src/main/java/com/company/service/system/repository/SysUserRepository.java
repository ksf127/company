package com.company.service.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.service.system.dto.PageResult;
import com.company.service.system.entity.SysRole;
import com.company.service.system.entity.SysUser;
import com.company.service.system.mapper.SysRoleMapper;
import com.company.service.system.mapper.SysRoleMenuMapper;
import com.company.service.system.mapper.SysUserMapper;
import com.company.service.system.mapper.SysUserRoleMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 持久化细节全部收敛在 repository：service 不出现 MyBatis-Plus 类型，
 * 由 ArchUnit 规则强制（见 ArchitectureTest）。
 */
@Repository
@RequiredArgsConstructor
public class SysUserRepository {

    private final SysUserMapper userMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysRoleMapper roleMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    public PageResult<SysUser> page(int pageNum, int pageSize, String userName, String status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(userName), SysUser::getUserName, userName)
                .eq(StringUtils.hasText(status), SysUser::getStatus, status)
                .orderByAsc(SysUser::getUserId);
        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public SysUser findById(Long userId) {
        return userMapper.selectById(userId);
    }

    public SysUser findByUserName(String userName) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)
                .last("LIMIT 1"));
    }

    public void insert(SysUser user) {
        userMapper.insert(user);
    }

    public void update(SysUser user) {
        userMapper.updateById(user);
    }

    public void deleteByIds(Collection<Long> userIds) {
        userMapper.deleteBatchIds(userIds);
    }

    public List<SysRole> findRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        return roleIds.isEmpty() ? List.of() : roleMapper.selectBatchIds(roleIds);
    }

    public List<String> findPermissions(Long userId) {
        return roleMenuMapper.selectPermsByUserId(userId);
    }

    public void replaceRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        for (Long roleId : roleIds) {
            userRoleMapper.insertUserRole(userId, roleId);
        }
    }

    public void deleteRoles(Collection<Long> userIds) {
        for (Long userId : userIds) {
            userRoleMapper.deleteByUserId(userId);
        }
    }

    public List<Long> findRoleIds(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    public void deleteByIds(Long[] userIds) {
        deleteByIds(Arrays.asList(userIds));
    }
}
