package com.photo.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户-角色关联表
 */
@Data
@TableName("t_sys_user_role")
public class UserRole {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long roleId;

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
