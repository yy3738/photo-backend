package com.photo.mvc.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LicenseCreateReq {

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
