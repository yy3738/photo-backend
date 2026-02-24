package com.photo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色
 */
@Getter
@AllArgsConstructor
public enum
UserRoleEnum {

    BUYER("buyer", "买家"),
    PHOTOGRAPHER("photographer", "摄影师"),
    ADMIN("admin", "管理员");

    private final String value;
    private final String desc;
}
