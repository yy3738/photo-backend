package com.photo.mvc.entity.vo;

import lombok.Data;

@Data
public class LoginVO {

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
