package com.photo.mvc.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLicenseReviewReq {

    @NotBlank(message = "操作不能为空")
    private String action;

    private String rejectReason;
}
