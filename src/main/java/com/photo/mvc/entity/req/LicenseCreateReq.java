package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "提交授权申请请求")
public class LicenseCreateReq {

    @Schema(description = "作品ID")
    @NotNull(message = "photoId不能为空")
    private Long photoId;

    @Schema(description = "授权用途")
    @NotBlank(message = "用途不能为空")
    private String purpose;

    @Schema(description = "使用场景")
    @NotBlank(message = "使用场景不能为空")
    private String scene;

    @Schema(description = "授权时长")
    @NotBlank(message = "使用期限不能为空")
    private String duration;

    @Schema(description = "联系方式")
    @NotBlank(message = "联系方式不能为空")
    private String contact;
}
