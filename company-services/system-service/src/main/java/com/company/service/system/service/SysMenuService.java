package com.company.service.system.service;

import com.company.common.core.BizException;
import com.company.common.core.ErrorCode;
import com.company.service.system.entity.SysMenu;
import com.company.service.system.repository.SysMenuRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuRepository menuRepository;

    public List<SysMenu> list() {
        return menuRepository.findAll();
    }

    public SysMenu get(Long menuId) {
        SysMenu menu = menuRepository.findById(menuId);
        if (menu == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "菜单不存在: " + menuId);
        }
        return menu;
    }

    public List<Long> menuIdsByUser(Long userId) {
        return menuRepository.findMenuIdsByUserId(userId);
    }

    @Transactional
    public SysMenu create(SysMenu menu) {
        menu.setMenuId(null);
        menu.setParentId(menu.getParentId() == null ? 0L : menu.getParentId());
        menu.setStatus(StringUtils.hasText(menu.getStatus()) ? menu.getStatus() : "0");
        menu.setCreateTime(LocalDateTime.now());
        menuRepository.insert(menu);
        return menu;
    }

    @Transactional
    public void update(SysMenu menu) {
        get(menu.getMenuId());
        if (menu.getMenuId().equals(menu.getParentId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "上级菜单不能是自己");
        }
        menuRepository.update(menu);
    }

    @Transactional
    public void delete(Long[] menuIds) {
        if (menuIds == null || menuIds.length == 0) {
            return;
        }
        menuRepository.deleteByIds(menuIds);
    }
}
