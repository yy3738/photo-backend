package com.photo.mvc.controller;

import com.photo.common.result.R;
import com.photo.mvc.entity.vo.CategoryVO;
import com.photo.mvc.entity.vo.TagVO;
import com.photo.mvc.service.SysCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "分类标签", description = "公开分类与标签查询")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final SysCategoryService sysCategoryService;

    @Operation(summary = "获取分类列表")
    @GetMapping
    public R<List<CategoryVO>> list() {
        return R.ok(sysCategoryService.listCategories());
    }

    @Operation(summary = "获取分类下的标签列表")
    @GetMapping("/{id}/tags")
    public R<List<TagVO>> listTags(@PathVariable Long id) {
        return R.ok(sysCategoryService.listTagsByCategoryId(id));
    }
}
