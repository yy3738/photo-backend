package com.photo.common.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;

/**
 * 分页查询基类
 */
@Data
public class PageQuery implements Serializable {

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Range(min = 1, max = 50, message = "每页条数范围1~50")
    private Integer pageSize = 20;

    public <T> Page<T> toMpPage() {
        return new Page<>(page, pageSize);
    }
}
