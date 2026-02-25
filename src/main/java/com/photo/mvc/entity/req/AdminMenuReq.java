package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "菜单新增/编辑请求")
public class AdminMenuReq {

    @Schema(description = "菜单名称")
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @Schema(description = "父级菜单ID")
    private Long parentId;

    @Schema(description = "类型：dir/menu/button")
    @NotBlank(message = "菜单类型不能为空")
    private String type;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sort;
}
