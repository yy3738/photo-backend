package com.photo.mvc.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_sys_menu")
public class SysMenuPO {

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
