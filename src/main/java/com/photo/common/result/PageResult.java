package com.photo.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private List<T> list;
    private Long total;
    private Integer page;
    private Integer pageSize;

    public static <T> PageResult<T> of(IPage<?> page, List<T> list) {
        return new PageResult<>(list, page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }
}
