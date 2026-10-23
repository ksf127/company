package com.company.service.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.ASSIGN_ID)
    private Long menuId;

    private String menuName;

    private Long parentId;

    private Integer orderNum;

    private String path;

    private String component;

    private String query;

    /** 0 是 / 1 否 */
    private String isFrame;

    /** 0 缓存 / 1 不缓存 */
    private String isCache;

    /** M 目录 / C 菜单 / F 按钮 */
    private String menuType;

    /** 0 显示 / 1 隐藏 */
    private String visible;

    private String status;

    /** 权限标识，例如 system:user:list */
    private String perms;

    private String icon;

    private LocalDateTime createTime;

    private String remark;
}
