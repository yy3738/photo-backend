package com.photo.mvc.entity.vo;

import lombok.Data;

@Data
public class RefreshTokenVO {

    private String accessToken;
    private String refreshToken;
}
