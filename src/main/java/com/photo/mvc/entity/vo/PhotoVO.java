package com.photo.mvc.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class PhotoVO {

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

    private Boolean isPurchased;

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
