package com.photo.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求
 */
@Data
public class WxLoginReqDTO {

    @NotBlank(message = "code不能为空")
    private String code;
}
