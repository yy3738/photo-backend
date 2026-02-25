package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "刷新Token请求")
public class RefreshTokenReq {

    @Schema(description = "刷新令牌")
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
