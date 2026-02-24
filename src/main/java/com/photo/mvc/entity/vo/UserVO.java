package com.photo.mvc.entity.vo;

import lombok.Data;

@Data
public class UserVO {

    private String id;
    private String openid;
    private String nickname;
    private String avatar;
    private String role;
    private Integer points;
    private Boolean isBanned;
    private String createdAt;
}
