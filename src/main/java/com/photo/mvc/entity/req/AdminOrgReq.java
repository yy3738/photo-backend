package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "组织新增/编辑请求")
public class AdminOrgReq {

    @Schema(description = "组织名称")
    @NotBlank(message = "组织名称不能为空")
    private String name;

    @Schema(description = "父级组织ID")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sort;
}
