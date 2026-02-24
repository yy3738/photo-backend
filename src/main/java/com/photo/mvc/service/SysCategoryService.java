package com.photo.mvc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.photo.mvc.entity.model.BizPhotoPO;
import com.photo.mvc.entity.model.SysCategoryPO;
import com.photo.mvc.entity.model.SysTagPO;
import com.photo.mvc.entity.vo.CategoryVO;
import com.photo.mvc.entity.vo.TagVO;
import com.photo.mvc.mapper.BizPhotoMapper;
import com.photo.mvc.mapper.SysCategoryMapper;
import com.photo.mvc.mapper.SysTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysCategoryService {

    private final SysCategoryMapper sysCategoryMapper;
    private final SysTagMapper sysTagMapper;
    private final BizPhotoMapper bizPhotoMapper;

    public List<CategoryVO> listCategories() {
        List<SysCategoryPO> categories = sysCategoryMapper.selectList(
                new LambdaQueryWrapper<SysCategoryPO>().orderByAsc(SysCategoryPO::getSort));

        return categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(String.valueOf(c.getId()));
            vo.setName(c.getName());
            vo.setSort(c.getSort());
            Long count = bizPhotoMapper.selectCount(
                    new LambdaQueryWrapper<BizPhotoPO>()
                            .eq(BizPhotoPO::getCategoryId, c.getId())
                            .eq(BizPhotoPO::getStatus, "approved"));
            vo.setPhotoCount(count);
            return vo;
        }).collect(Collectors.toList());
    }

    public List<TagVO> listTagsByCategoryId(Long categoryId) {
        List<SysTagPO> tags = sysTagMapper.selectList(
                new LambdaQueryWrapper<SysTagPO>()
                        .eq(SysTagPO::getCategoryId, categoryId)
                        .orderByAsc(SysTagPO::getSort));

        return tags.stream().map(t -> {
            TagVO vo = new TagVO();
            vo.setId(String.valueOf(t.getId()));
            vo.setName(t.getName());
            vo.setCategoryId(String.valueOf(t.getCategoryId()));
            vo.setSort(t.getSort());
            return vo;
        }).collect(Collectors.toList());
    }
}
