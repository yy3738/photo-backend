package com.photo.module.category.controller;

import com.photo.common.result.R;
import com.photo.module.category.dto.CategoryRespDTO;
import com.photo.module.category.dto.TagRespDTO;
import com.photo.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public R<List<CategoryRespDTO>> list() {
        return R.ok(categoryService.listCategories());
    }

    @GetMapping("/{id}/tags")
    public R<List<TagRespDTO>> listTags(@PathVariable Long id) {
        return R.ok(categoryService.listTagsByCategoryId(id));
    }
}
