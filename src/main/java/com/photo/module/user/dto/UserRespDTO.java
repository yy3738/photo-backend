package com.photo.module.user.dto;

import lombok.Data;

/**
 * 用户信息响应
 */
@Data
public class UserRespDTO {

    private String id;
    private String openid;
    private String nickname;
    private String avatar;
    private String role;
    private Integer points;
    private Boolean isBanned;
    private String createdAt;
}
