package com.photo.mvc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.photo.mvc.entity.model.BizPhotoPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizPhotoMapper extends BaseMapper<BizPhotoPO> {
}
