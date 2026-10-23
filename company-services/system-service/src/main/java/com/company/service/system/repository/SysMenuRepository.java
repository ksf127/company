package com.company.service.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.service.system.entity.SysMenu;
import com.company.service.system.mapper.SysMenuMapper;
import com.company.service.system.mapper.SysRoleMenuMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SysMenuRepository {

    private final SysMenuMapper menuMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    public List<SysMenu> findAll() {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum));
    }

    public SysMenu findById(Long menuId) {
        return menuMapper.selectById(menuId);
    }

    public void insert(SysMenu menu) {
        menuMapper.insert(menu);
    }

    public void update(SysMenu menu) {
        menuMapper.updateById(menu);
    }

    public void deleteByIds(Long[] menuIds) {
        menuMapper.deleteBatchIds(Arrays.asList(menuIds));
        for (Long menuId : menuIds) {
            roleMenuMapper.deleteByMenuId(menuId);
        }
    }

    public List<Long> findMenuIdsByUserId(Long userId) {
        return roleMenuMapper.selectMenuIdsByUserId(userId);
    }
}
