package com.photo.module.upload.dto;

import lombok.Data;

/**
 * OSS 上传签名响应
 */
@Data
public class UploadSignatureRespDTO {

    private String host;
    private String key;
    private String policy;
    private String accessId;
    private String signature;
    private String expireAt;
}
