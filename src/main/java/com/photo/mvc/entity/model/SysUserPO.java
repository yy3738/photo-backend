package com.photo.mvc.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_sys_user")
public class SysUserPO {

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
