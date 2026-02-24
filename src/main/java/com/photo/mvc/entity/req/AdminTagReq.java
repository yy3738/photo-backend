package com.photo.mvc.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminTagReq {

    @NotBlank(message = "标签名称不能为空")
    private String name;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private Integer sort;
}
