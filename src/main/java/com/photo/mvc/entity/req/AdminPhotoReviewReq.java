package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "管理员作品审核请求")
public class AdminPhotoReviewReq {

    @Schema(description = "操作：approve/reject")
    @NotBlank(message = "操作不能为空")
    private String action;

    @Schema(description = "驳回原因")
    private String rejectReason;
}
