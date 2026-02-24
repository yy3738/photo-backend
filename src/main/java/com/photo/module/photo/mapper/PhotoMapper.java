package com.photo.module.photo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.photo.module.photo.entity.Photo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PhotoMapper extends BaseMapper<Photo> {
}
