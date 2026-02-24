package com.photo.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
@TableName("t_biz_order")
public class Order {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long photoId;
    private Long photographerId;
    private String photoTitle;
    private String photoPreviewUrl;
    private Integer price;
    private Integer platformFee;
    private Integer photographerEarned;

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
