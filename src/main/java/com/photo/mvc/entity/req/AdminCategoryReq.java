package com.photo.mvc.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCategoryReq {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sort;
}
