package com.company.api.system;

public record SysMenuDTO(
        Long menuId,
        Long parentId,
        String menuName,
        String path,
        String component,
        String perms,
        String menuType,
        Integer orderNum,
        String visible,
        String status) {
}
