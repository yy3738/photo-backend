package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分类信息")
public class CategoryVO {

    @Schema(description = "分类ID")
    private String id;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "排序号")
    private Integer sort;
    @Schema(description = "作品数量")
    private Long photoCount;
}
