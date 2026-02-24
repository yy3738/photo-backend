package com.photo.mvc.controller;

import com.photo.common.result.R;
import com.photo.mvc.entity.vo.CategoryVO;
import com.photo.mvc.entity.vo.TagVO;
import com.photo.mvc.service.SysCategoryService;
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

    private final SysCategoryService sysCategoryService;

    @GetMapping
    public R<List<CategoryVO>> list() {
        return R.ok(sysCategoryService.listCategories());
    }

    @GetMapping("/{id}/tags")
    public R<List<TagVO>> listTags(@PathVariable Long id) {
        return R.ok(sysCategoryService.listTagsByCategoryId(id));
    }
}
