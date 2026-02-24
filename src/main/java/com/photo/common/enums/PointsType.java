package com.photo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分流水类型
 */
@Getter
@AllArgsConstructor
public enum PointsType {

    EARN("earn", "收入"),
    SPEND("spend", "支出");

    private final String value;
    private final String desc;
}
