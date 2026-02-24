package com.photo.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员管理分类请求
 */
@Data
public class AdminCategoryReqDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sort;
}
