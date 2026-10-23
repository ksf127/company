package com.company.service.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 若依 sys_user 的兼容映射：字段名与若依一致，逻辑删除改用平台统一的 deleted。
 * password 永不序列化到 HTTP 响应。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    private Long deptId;

    private String userName;

    private String nickName;

    private String userType;

    private String email;

    private String phonenumber;

    private String sex;

    private String avatar;

    /** 只允许写入，永不返回给前端 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 0 正常 / 1 停用，与若依一致 */
    private String status;

    @TableLogic
    private Integer deleted;

    private String loginIp;

    private LocalDateTime loginDate;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    private String remark;
}
