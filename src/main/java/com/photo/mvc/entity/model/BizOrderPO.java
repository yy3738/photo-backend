package com.photo.mvc.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_biz_order")
@Schema(description = "订单表")
public class BizOrderPO {

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "买家用户ID")
    private Long userId;
    @Schema(description = "作品ID")
    private Long photoId;
    @Schema(description = "摄影师用户ID")
    private Long photographerId;
    @Schema(description = "作品标题")
    private String photoTitle;
    @Schema(description = "作品预览图URL")
    private String photoPreviewUrl;
    @Schema(description = "成交价格（积分）")
    private Integer price;
    @Schema(description = "平台手续费（积分）")
    private Integer platformFee;
    @Schema(description = "摄影师实得（积分）")
    private Integer photographerEarned;

    @Schema(description = "创建人ID")
    private Long createBy;
    @Schema(description = "更新人ID")
    private Long updateBy;

    @Schema(description = "乐观锁版本号")
    @Version
    private Integer version;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除：0未删 1已删")
    @TableLogic
    private Integer isDeleted;
}
