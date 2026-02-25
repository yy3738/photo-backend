package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信登录请求")
public class WxLoginReq {

    @Schema(description = "微信临时登录凭证code")
    @NotBlank(message = "code不能为空")
    private String code;
}
