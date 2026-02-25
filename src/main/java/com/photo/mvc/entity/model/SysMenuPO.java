package com.photo.mvc.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_sys_menu")
@Schema(description = "菜单表")
public class SysMenuPO {

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "菜单名称")
    private String name;
    @Schema(description = "父级菜单ID")
    private Long parentId;
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

    @Schema(description = "创建人ID")
    private Long createBy;
    @Schema(description = "更新人ID")
    private Long updateBy;

    @Schema(description = "乐观锁版本号")
    @Version
    private Integer version;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除：0未删 1已删")
    @TableLogic
    private Integer isDeleted;
}
