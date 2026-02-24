package com.photo.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核作品请求
 */
@Data
public class AdminPhotoReviewReqDTO {

    @NotBlank(message = "操作不能为空")
    private String action; // approve / reject

    private String rejectReason;
}
