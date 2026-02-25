package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员标签请求")
public class AdminTagReq {

    @Schema(description = "标签名称")
    @NotBlank(message = "标签名称不能为空")
    private String name;

    @Schema(description = "所属分类ID")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Schema(description = "排序号")
    private Integer sort;
}
