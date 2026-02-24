package com.photo.module.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作品-标签关联表
 */
@Data
@TableName("t_biz_photo_tag")
public class PhotoTag {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long photoId;
    private Long tagId;

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
