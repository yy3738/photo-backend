package com.photo.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求
 */
@Data
public class RefreshTokenReqDTO {

    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
