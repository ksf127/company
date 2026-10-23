package com.company.service.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.ASSIGN_ID)
    private Long roleId;

    private String roleName;

    /** 权限标识前缀，例如 admin、common */
    private String roleKey;

    private Integer roleSort;

    /** 1 全部数据 / 2 自定义 / 3 本部门 / 4 本部门及以下 / 5 仅本人 */
    private String dataScope;

    private String menuCheckStrictly;

    private String deptCheckStrictly;

    private String status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private String remark;
}
