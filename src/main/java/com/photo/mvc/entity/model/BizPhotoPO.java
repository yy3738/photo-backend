package com.photo.mvc.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_biz_photo")
@Schema(description = "作品表")
public class BizPhotoPO {

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "作品标题")
    private String title;
    @Schema(description = "作品描述")
    private String description;
    @Schema(description = "预览图OSS Key")
    private String previewKey;
    @Schema(description = "原图OSS Key")
    private String originalKey;
    @Schema(description = "分类ID")
    private Long categoryId;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "上传用户ID")
    private Long userId;
    @Schema(description = "价格（积分）")
    private Integer price;
    @Schema(description = "是否允许授权：0否 1是")
    private Integer allowLicense;
    @Schema(description = "状态：pending/approved/rejected")
    private String status;
    @Schema(description = "驳回原因")
    private String rejectReason;
    @Schema(description = "购买次数")
    private Integer purchaseCount;

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
