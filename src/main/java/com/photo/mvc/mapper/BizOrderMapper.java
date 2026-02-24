package com.photo.mvc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.photo.mvc.entity.model.BizOrderPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizOrderMapper extends BaseMapper<BizOrderPO> {
}
