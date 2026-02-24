package com.photo.module.category.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签表
 */
@Data
@TableName("t_sys_tag")
public class Tag {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private Long categoryId;
    private Integer sort;

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
