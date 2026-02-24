package com.photo.module.studio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 上传/编辑作品请求
 */
@Data
public class StudioPhotoReqDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "预览图URL不能为空")
    private String previewUrl;

    @NotBlank(message = "原图Key不能为空")
    private String originalKey;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private List<Long> tagIds;

    @NotNull(message = "价格不能为空")
    private Integer price;

    private Integer allowLicense;
}
