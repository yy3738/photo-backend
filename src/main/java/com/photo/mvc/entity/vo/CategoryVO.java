package com.photo.mvc.entity.vo;

import lombok.Data;

@Data
public class CategoryVO {

    private String id;
    private String name;
    private Integer sort;
    private Long photoCount;
}
