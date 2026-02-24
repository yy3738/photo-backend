package com.photo.module.studio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核授权申请请求（摄影师审批）
 */
@Data
public class StudioLicenseReviewReqDTO {

    @NotBlank(message = "操作不能为空")
    private String action; // approve / reject

    private String rejectReason;
}
