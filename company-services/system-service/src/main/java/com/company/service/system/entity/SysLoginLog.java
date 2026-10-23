package com.company.service.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_logininfor")
public class SysLoginLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long infoId;

    private String userName;

    private String ipaddr;

    private String browser;

    private String os;

    /** 0 成功 / 1 失败 */
    private String status;

    private String msg;

    private LocalDateTime loginTime;
}
