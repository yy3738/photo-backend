package com.photo.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核授权申请请求（管理员初审）
 */
@Data
public class AdminLicenseReviewReqDTO {

    @NotBlank(message = "操作不能为空")
    private String action; // approve / reject

    private String rejectReason;
}
