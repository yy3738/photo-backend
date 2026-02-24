package com.photo.mvc.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StudioPhotoReq {

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
