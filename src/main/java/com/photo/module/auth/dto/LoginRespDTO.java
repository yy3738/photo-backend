package com.photo.module.auth.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginRespDTO {

    private String accessToken;
    private String refreshToken;
    private LoginUserInfo user;

    @Data
    public static class LoginUserInfo {
        private String id;
        private String nickname;
        private String avatar;
        private String role;
        private Integer points;
    }
}
