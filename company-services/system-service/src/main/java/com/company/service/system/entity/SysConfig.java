package com.company.service.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.ASSIGN_ID)
    private Long configId;

    private String configName;

    private String configKey;

    private String configValue;

    /** Y 系统内置（不允许删除） / N 否 */
    private String configType;

    private String remark;

    private LocalDateTime createTime;
}
