package com.photo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 授权用途
 */
@Getter
@AllArgsConstructor
public enum LicensePurpose {

    COMMERCIAL("commercial", "商业广告"),
    NEWS("news", "新闻媒体"),
    PERSONAL("personal", "个人创作"),
    EDUCATION("education", "教育出版"),
    OTHER("other", "其他");

    private final String value;
    private final String desc;
}
