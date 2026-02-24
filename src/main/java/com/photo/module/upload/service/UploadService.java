package com.photo.module.upload.service;

import com.photo.module.upload.dto.UploadSignatureRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class UploadService {

    @Value("${oss.host:https://bucket.oss-cn-hangzhou.aliyuncs.com}")
    private String ossHost;

    @Value("${oss.access-key-id:}")
    private String accessKeyId;

    @Value("${oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${oss.bucket:photo-bucket}")
    private String bucket;

    /**
     * 获取 OSS 直传签名
     */
    public UploadSignatureRespDTO getSignature(String filename, String type) {
        // 生成文件路径: photos/2026/02/uuid_type.ext
        String ext = "";
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx > 0) {
            ext = filename.substring(dotIdx);
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String key = "photos/" + datePath + "/" + uuid + "_" + type + ext;

        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(30);

        // 构建 policy（简化实现，实际应使用 OSS SDK）
        String policyJson = String.format(
                "{\"expiration\":\"%s\",\"conditions\":[[\"content-length-range\",0,52428800],[\"eq\",\"$key\",\"%s\"]]}",
                expireAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z",
                key);
        String policy = Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));

        // 签名（简化实现，实际应使用 HMAC-SHA1 + accessKeySecret）
        String signature = "mock_signature_" + uuid.substring(0, 8);

        UploadSignatureRespDTO resp = new UploadSignatureRespDTO();
        resp.setHost(ossHost);
        resp.setKey(key);
        resp.setPolicy(policy);
        resp.setAccessId(accessKeyId);
        resp.setSignature(signature);
        resp.setExpireAt(expireAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return resp;
    }
}
