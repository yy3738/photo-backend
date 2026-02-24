package com.photo.mvc.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_biz_license")
public class BizLicensePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long photoId;
    private String photoTitle;
    private String photoPreviewUrl;
    private Long applicantId;
    private Long photographerId;
    private String purpose;
    private String scene;
    private String duration;
    private String contact;
    private String status;
    private String rejectReason;
    private String certificateUrl;

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
