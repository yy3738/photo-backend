package com.photo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型
 */
@Getter
@AllArgsConstructor
public enum MenuType {

    DIR("dir", "目录"),
    MENU("menu", "菜单"),
    BUTTON("button", "按钮");

    private final String value;
    private final String desc;
}
