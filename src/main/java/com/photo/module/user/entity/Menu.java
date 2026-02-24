package com.photo.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单表（目录/菜单/按钮三级）
 */
@Data
@TableName("t_sys_menu")
public class Menu {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private Long parentId;
    private String type;
    private String path;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer status;

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
