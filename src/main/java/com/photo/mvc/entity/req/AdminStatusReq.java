package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "通用启用/禁用请求")
public class AdminStatusReq {

    @Schema(description = "状态：0禁用 1启用")
    @NotNull(message = "状态不能为空")
    private Integer status;
}
