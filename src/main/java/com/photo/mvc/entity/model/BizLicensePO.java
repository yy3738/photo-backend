package com.photo.mvc.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_biz_license")
@Schema(description = "授权申请表")
public class BizLicensePO {

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "作品ID")
    private Long photoId;
    @Schema(description = "作品标题")
    private String photoTitle;
    @Schema(description = "作品预览图OSS Key")
    private String photoPreviewKey;
    @Schema(description = "申请人ID")
    private Long applicantId;
    @Schema(description = "摄影师ID")
    private Long photographerId;
    @Schema(description = "授权用途")
    private String purpose;
    @Schema(description = "使用场景")
    private String scene;
    @Schema(description = "授权时长")
    private String duration;
    @Schema(description = "联系方式")
    private String contact;
    @Schema(description = "状态：pending_admin/pending_photographer/approved/rejected")
    private String status;
    @Schema(description = "驳回原因")
    private String rejectReason;
    @Schema(description = "授权证书URL")
    private String certificateUrl;

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
