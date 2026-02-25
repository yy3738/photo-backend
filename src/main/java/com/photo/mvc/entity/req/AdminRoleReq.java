package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色新增/编辑请求")
public class AdminRoleReq {

    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    private String name;

    @Schema(description = "角色编码")
    @NotBlank(message = "角色编码不能为空")
    private String code;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;
}
