package com.photo.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员管理标签请求
 */
@Data
public class AdminTagReqDTO {

    @NotBlank(message = "标签名称不能为空")
    private String name;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private Integer sort;
}
