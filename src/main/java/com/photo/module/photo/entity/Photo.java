package com.photo.module.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作品表
 */
@Data
@TableName("t_biz_photo")
public class Photo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;
    private String description;
    private String previewUrl;
    private String originalKey;
    private Long categoryId;
    private String categoryName;
    private Long userId;
    private Integer price;
    private Integer allowLicense;
    private String status;
    private String rejectReason;
    private Integer purchaseCount;

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
