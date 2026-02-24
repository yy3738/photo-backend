package com.photo.module.photo.dto;

import lombok.Data;

/**
 * 下载链接响应
 */
@Data
public class DownloadRespDTO {

    private String url;
    private String expireAt;
}
