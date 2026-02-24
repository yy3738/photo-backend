package com.photo.module.photo.dto;

import lombok.Data;

import java.util.List;

/**
 * 作品列表/详情响应
 */
@Data
public class PhotoRespDTO {

    private String id;
    private String title;
    private String description;
    private String previewUrl;
    private String categoryId;
    private String categoryName;
    private List<TagItem> tags;
    private Integer price;
    private Boolean allowLicense;
    private String status;
    private String rejectReason;
    private PhotographerInfo photographer;
    private Integer purchaseCount;
    private String createdAt;
    private String updatedAt;

    /** 详情接口额外字段 */
    private Boolean isPurchased;

    /** 摄影师工作台额外字段 */
    private String originalKey;

    @Data
    public static class TagItem {
        private String id;
        private String name;
    }

    @Data
    public static class PhotographerInfo {
        private String id;
        private String nickname;
        private String avatar;
    }
}
