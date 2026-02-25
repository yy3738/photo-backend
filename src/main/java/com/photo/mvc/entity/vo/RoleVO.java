package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色信息")
public class RoleVO {

    @Schema(description = "角色ID")
    private String id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "关联菜单ID列表")
    private List<String> menuIds;

    @Schema(description = "创建时间")
    private String createdAt;
}
