package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "刷新Token结果")
public class RefreshTokenVO {

    @Schema(description = "新的访问令牌")
    private String accessToken;
    @Schema(description = "新的刷新令牌")
    private String refreshToken;
}
