package com.company.service.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict_type")
public class SysDictType {

    @TableId(type = IdType.ASSIGN_ID)
    private Long dictId;

    private String dictName;

    /** 字典类型编码，例如 sys_user_sex */
    private String dictType;

    private String status;

    private String remark;

    private LocalDateTime createTime;
}
