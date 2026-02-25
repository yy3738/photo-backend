package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "组织树节点")
public class OrgVO {

    @Schema(description = "组织ID")
    private String id;

    @Schema(description = "组织名称")
    private String name;

    @Schema(description = "父级组织ID")
    private String parentId;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;

    @Schema(description = "子组织列表")
    private List<OrgVO> children;
}
