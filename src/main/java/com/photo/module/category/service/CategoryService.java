package com.photo.module.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.photo.module.category.dto.CategoryRespDTO;
import com.photo.module.category.dto.TagRespDTO;
import com.photo.module.category.entity.Category;
import com.photo.module.category.entity.Tag;
import com.photo.module.category.mapper.CategoryMapper;
import com.photo.module.category.mapper.TagMapper;
import com.photo.module.photo.mapper.PhotoMapper;
import com.photo.module.photo.entity.Photo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final PhotoMapper photoMapper;

    /**
     * 获取所有分类（含作品数量）
     */
    public List<CategoryRespDTO> listCategories() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));

        return categories.stream().map(c -> {
            CategoryRespDTO dto = new CategoryRespDTO();
            dto.setId(String.valueOf(c.getId()));
            dto.setName(c.getName());
            dto.setSort(c.getSort());
            // 统计该分类下已审核通过的作品数
            Long count = photoMapper.selectCount(
                    new LambdaQueryWrapper<Photo>()
                            .eq(Photo::getCategoryId, c.getId())
                            .eq(Photo::getStatus, "approved"));
            dto.setPhotoCount(count);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取指定分类下的标签
     */
    public List<TagRespDTO> listTagsByCategoryId(Long categoryId) {
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getCategoryId, categoryId)
                        .orderByAsc(Tag::getSort));

        return tags.stream().map(t -> {
            TagRespDTO dto = new TagRespDTO();
            dto.setId(String.valueOf(t.getId()));
            dto.setName(t.getName());
            dto.setCategoryId(String.valueOf(t.getCategoryId()));
            dto.setSort(t.getSort());
            return dto;
        }).collect(Collectors.toList());
    }
}
