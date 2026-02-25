package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标签信息")
public class TagVO {

    @Schema(description = "标签ID")
    private String id;
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "所属分类ID")
    private String categoryId;
    @Schema(description = "排序号")
    private Integer sort;
}
