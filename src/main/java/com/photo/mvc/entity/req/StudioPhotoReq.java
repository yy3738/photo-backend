package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "摄影师上传/编辑作品请求")
public class StudioPhotoReq {

    @Schema(description = "作品标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "作品描述")
    private String description;

    @Schema(description = "预览图URL")
    @NotBlank(message = "预览图URL不能为空")
    private String previewUrl;

    @Schema(description = "原图OSS Key")
    @NotBlank(message = "原图Key不能为空")
    private String originalKey;

    @Schema(description = "分类ID")
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "价格（积分），范围10~9999")
    @NotNull(message = "价格不能为空")
    private Integer price;

    @Schema(description = "是否允许授权：0否 1是")
    private Integer allowLicense;
}
