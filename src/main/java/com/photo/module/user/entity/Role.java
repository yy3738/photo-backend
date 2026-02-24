package com.photo.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色表
 */
@Data
@TableName("t_sys_role")
public class Role {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private String code;
    private Integer sort;
    private Integer status;
    private String remark;

    private Long createBy;
    private Long updateBy;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
