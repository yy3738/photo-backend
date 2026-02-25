package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录结果")
public class LoginVO {

    @Schema(description = "访问令牌")
    private String accessToken;
    @Schema(description = "刷新令牌")
    private String refreshToken;
    @Schema(description = "用户信息")
    private LoginUserInfo user;

    @Data
    @Schema(description = "登录用户信息")
    public static class LoginUserInfo {
        @Schema(description = "用户ID")
        private String id;
        @Schema(description = "昵称")
        private String nickname;
        @Schema(description = "头像URL")
        private String avatar;
        @Schema(description = "角色")
        private String role;
        @Schema(description = "积分余额")
        private Integer points;
    }
}
