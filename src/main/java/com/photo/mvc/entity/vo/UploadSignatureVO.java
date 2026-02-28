package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "OSS上传签名信息")
public class UploadSignatureVO {

    @Schema(description = "MinIO服务地址（不含协议）")
    private String host;

    @Schema(description = "文件存储路径（含文件名）")
    private String key;

    @Schema(description = "表单数据")
    private Map<String, String> formData;
}
