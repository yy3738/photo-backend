package com.photo.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    @Schema(description = "数据列表")
    private List<T> list;
    @Schema(description = "总记录数")
    private Long total;
    @Schema(description = "当前页码")
    private Integer page;
    @Schema(description = "每页条数")
    private Integer pageSize;

    public static <T> PageResult<T> of(IPage<?> page, List<T> list) {
        return new PageResult<>(list, page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }
}
