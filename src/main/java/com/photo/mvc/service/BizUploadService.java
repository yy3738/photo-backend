package com.photo.mvc.service;

import com.photo.common.config.MinIOConfig;
import com.photo.common.config.MinioTemplate;
import com.photo.mvc.entity.vo.UploadSignatureVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizUploadService {

    private final MinIOConfig minIOConfig;
    private final MinioTemplate minioTemplate;

    /**
     * 获取上传签名信息
     * 返回预签名POST表单数据，前端使用 formData 方式上传
     */
    public UploadSignatureVO getSignature(String filename, String type) {
        String ext = "";
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx > 0) {
            ext = filename.substring(dotIdx);
        }

        // 生成存储Key
        String key = minioTemplate.generateKey(type, ext);

        // 获取MinIO服务器地址（去掉协议前缀）
        String host = minIOConfig.getEndpoint().replace("http://", "").replace("https://", "");

        // 获取预签名POST表单数据
        Map<String, String> formData = minioTemplate.getPresignedPostFormData(key);

        UploadSignatureVO vo = new UploadSignatureVO();
        vo.setHost(host);
        vo.setKey(key);
        vo.setFormData(formData);
        return vo;
    }
}
