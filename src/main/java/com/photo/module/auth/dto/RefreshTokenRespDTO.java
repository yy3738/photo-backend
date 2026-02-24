package com.photo.module.auth.dto;

import lombok.Data;

/**
 * 刷新Token响应
 */
@Data
public class RefreshTokenRespDTO {

    private String accessToken;
    private String refreshToken;
}
