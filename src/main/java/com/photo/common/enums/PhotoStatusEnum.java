package com.photo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 作品状态
 */
@Getter
@AllArgsConstructor
public enum PhotoStatusEnum {

    PENDING("pending", "待审核"),
    APPROVED("approved", "已上架"),
    REJECTED("rejected", "已拒绝"),
    OFFLINE("offline", "已下架");

    private final String value;
    private final String desc;
}
