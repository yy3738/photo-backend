package com.photo.mvc.entity.vo;

import lombok.Data;

@Data
public class TagVO {

    private String id;
    private String name;
    private String categoryId;
    private Integer sort;
}
