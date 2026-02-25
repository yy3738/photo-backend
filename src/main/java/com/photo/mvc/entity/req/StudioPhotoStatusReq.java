package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "作品上架/下架请求")
public class StudioPhotoStatusReq {

    @Schema(description = "目标状态：approved（上架）/ offline（下架）")
    @NotBlank(message = "状态不能为空")
    private String status;
}
