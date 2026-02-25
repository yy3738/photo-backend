package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "OSS上传签名信息")
public class UploadSignatureVO {

    @Schema(description = "OSS上传地址")
    private String host;
    @Schema(description = "文件Key")
    private String key;
    @Schema(description = "上传策略")
    private String policy;
    @Schema(description = "AccessKey ID")
    private String accessId;
    @Schema(description = "签名")
    private String signature;
    @Schema(description = "过期时间")
    private String expireAt;
}
