package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "菜单树节点")
public class MenuVO {

    @Schema(description = "菜单ID")
    private String id;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "父级菜单ID")
    private String parentId;

    @Schema(description = "类型：dir/menu/button")
    private String type;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;

    @Schema(description = "子菜单列表")
    private List<MenuVO> children;
}
