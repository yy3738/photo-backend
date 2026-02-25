package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "下载链接信息")
public class DownloadVO {

    @Schema(description = "签名下载URL")
    private String url;
    @Schema(description = "过期时间")
    private String expireAt;
}
