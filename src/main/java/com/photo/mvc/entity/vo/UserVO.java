package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID")
    private String id;
    @Schema(description = "微信openid")
    private String openid;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像URL")
    private String avatar;
    @Schema(description = "角色：buyer/photographer/admin")
    private String role;
    @Schema(description = "积分余额")
    private Integer points;
    @Schema(description = "是否封禁")
    private Boolean isBanned;
    @Schema(description = "注册时间")
    private String createdAt;
}
