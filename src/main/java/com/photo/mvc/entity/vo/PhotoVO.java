package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "作品信息")
public class PhotoVO {

    @Schema(description = "作品ID")
    private String id;
    @Schema(description = "作品标题")
    private String title;
    @Schema(description = "作品描述")
    private String description;
    @Schema(description = "预览图URL")
    private String previewUrl;
    @Schema(description = "分类ID")
    private String categoryId;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "标签列表")
    private List<TagItem> tags;
    @Schema(description = "价格（积分）")
    private Integer price;
    @Schema(description = "是否允许授权")
    private Boolean allowLicense;
    @Schema(description = "状态：pending/approved/rejected")
    private String status;
    @Schema(description = "驳回原因")
    private String rejectReason;
    @Schema(description = "摄影师信息")
    private PhotographerInfo photographer;
    @Schema(description = "购买次数")
    private Integer purchaseCount;
    @Schema(description = "创建时间")
    private String createdAt;
    @Schema(description = "更新时间")
    private String updatedAt;

    @Schema(description = "当前用户是否已购买")
    private Boolean isPurchased;

    @Schema(description = "原图OSS Key")
    private String originalKey;

    @Data
    @Schema(description = "标签项")
    public static class TagItem {
        @Schema(description = "标签ID")
        private String id;
        @Schema(description = "标签名称")
        private String name;
    }

    @Data
    @Schema(description = "摄影师信息")
    public static class PhotographerInfo {
        @Schema(description = "摄影师ID")
        private String id;
        @Schema(description = "摄影师昵称")
        private String nickname;
        @Schema(description = "摄影师头像")
        private String avatar;
    }
}
