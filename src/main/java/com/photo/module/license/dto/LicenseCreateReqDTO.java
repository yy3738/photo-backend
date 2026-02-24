package com.photo.module.license.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交授权申请请求
 */
@Data
public class LicenseCreateReqDTO {

    @NotNull(message = "photoId不能为空")
    private Long photoId;

    @NotBlank(message = "用途不能为空")
    private String purpose;

    @NotBlank(message = "使用场景不能为空")
    private String scene;

    @NotBlank(message = "使用期限不能为空")
    private String duration;

    @NotBlank(message = "联系方式不能为空")
    private String contact;
}
