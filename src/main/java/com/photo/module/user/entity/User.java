package com.photo.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("t_sys_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String openid;
    private String nickname;
    private String avatar;
    private String role;
    private Integer points;
    private Integer isBanned;
    private Long orgId;

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
