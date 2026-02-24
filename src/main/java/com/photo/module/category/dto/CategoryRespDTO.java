package com.photo.module.category.dto;

import lombok.Data;

/**
 * 分类响应
 */
@Data
public class CategoryRespDTO {

    private String id;
    private String name;
    private Integer sort;
    private Long photoCount;
}
