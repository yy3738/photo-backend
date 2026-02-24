package com.photo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 授权申请状态
 */
@Getter
@AllArgsConstructor
public enum LicenseStatus {

    PENDING_ADMIN("pending_admin", "待管理员审核"),
    PENDING_PHOTOGRAPHER("pending_photographer", "待摄影师确认"),
    APPROVED("approved", "已授权"),
    REJECTED("rejected", "已拒绝");

    private final String value;
    private final String desc;
}
