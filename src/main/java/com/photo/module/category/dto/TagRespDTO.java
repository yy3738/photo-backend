package com.photo.module.category.dto;

import lombok.Data;

/**
 * 标签响应
 */
@Data
public class TagRespDTO {

    private String id;
    private String name;
    private String categoryId;
    private Integer sort;
}
