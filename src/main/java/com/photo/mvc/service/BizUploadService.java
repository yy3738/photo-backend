package com.photo.mvc.service;

import com.photo.mvc.entity.vo.UploadSignatureVO;
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
public class BizUploadService {

    @Value("${oss.host:https://bucket.oss-cn-hangzhou.aliyuncs.com}")
    private String ossHost;

    @Value("${oss.access-key-id:}")
    private String accessKeyId;

    @Value("${oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${oss.bucket:photo-bucket}")
    private String bucket;

    public UploadSignatureVO getSignature(String filename, String type) {
        String ext = "";
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx > 0) {
            ext = filename.substring(dotIdx);
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String key = "photos/" + datePath + "/" + uuid + "_" + type + ext;

        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(30);

        String policyJson = String.format(
                "{\"expiration\":\"%s\",\"conditions\":[[\"content-length-range\",0,52428800],[\"eq\",\"$key\",\"%s\"]]}",
                expireAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z",
                key);
        String policy = Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));

        String signature = "mock_signature_" + uuid.substring(0, 8);

        UploadSignatureVO vo = new UploadSignatureVO();
        vo.setHost(ossHost);
        vo.setKey(key);
        vo.setPolicy(policy);
        vo.setAccessId(accessKeyId);
        vo.setSignature(signature);
        vo.setExpireAt(expireAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return vo;
    }
}
