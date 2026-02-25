package com.photo.mvc.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_biz_points_record")
@Schema(description = "积分流水表")
public class BizPointsRecordPO {

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "类型：recharge/purchase/earn/refund")
    private String type;
    @Schema(description = "变动数量")
    private Integer amount;
    @Schema(description = "变动后余额")
    private Integer balance;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "关联订单ID")
    private Long relatedOrderId;
    @Schema(description = "关联作品ID")
    private Long relatedPhotoId;
    @Schema(description = "关联作品标题")
    private String relatedPhotoTitle;

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
