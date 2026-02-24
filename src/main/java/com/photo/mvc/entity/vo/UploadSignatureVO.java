package com.photo.mvc.entity.vo;

import lombok.Data;

@Data
public class UploadSignatureVO {

    private String host;
    private String key;
    private String policy;
    private String accessId;
    private String signature;
    private String expireAt;
}
