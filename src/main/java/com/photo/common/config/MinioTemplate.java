package com.photo.common.config;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioTemplate {

    private final MinIOConfig minIOConfig;
    private final MinioClient minioClient;

    /**
     * 生成预签名上传URL
     */
    public String getPresignedUploadUrl(String key) {
        try {
            // 确保bucket存在
            ensureBucketExists();

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(key)
                            .method(io.minio.http.Method.PUT)
                            .expiry(7 * 24 * 60 * 60) // 7天有效期（秒）
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名上传URL失败", e);
            throw new RuntimeException("生成预签名上传URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成预签名POST上传的表单数据
     * 用于前端 formData 方式上传
     *
     * @param key 文件存储路径（含文件名）
     * @return 包含 policyBase64, algorithm, credential, date, expire, signedHeaders, signature 的Map
     */
    public Map<String, String> getPresignedPostFormData(String key) {
        try {
            // 确保bucket存在
            ensureBucketExists();

            // 创建PostPolicy (bucket, expiration)
            ZonedDateTime expiration = ZonedDateTime.now().plusDays(7);
            PostPolicy policy = new PostPolicy(minIOConfig.getBucket(), expiration);

            // 添加object key精确匹配条件
            policy.addEqualsCondition("key", key);

            // 生成表单数据
            return minioClient.getPresignedPostFormData(policy);
        } catch (Exception e) {
            log.error("生成预签名POST表单数据失败", e);
            throw new RuntimeException("生成预签名POST表单数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成预签名下载URL
     */
    public String getPresignedDownloadUrl(String key, int expireMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(key)
                            .method(io.minio.http.Method.GET)
                            .expiry(expireMinutes * 60) // 转换为秒
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名下载URL失败", e);
            throw new RuntimeException("生成预签名下载URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证文件是否存在
     */
    public boolean exists(String key) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(key)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件信息
     */
    public StatObjectResponse getObjectStat(String key) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", key, e);
            throw new RuntimeException("获取文件信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     */
    public void delete(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("删除文件失败: {}", key, e);
            throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 确保Bucket存在，不存在则创建
     */
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minIOConfig.getBucket())
                                .build()
                );
                log.info("创建MinIO Bucket: {}", minIOConfig.getBucket());
            }
        } catch (Exception e) {
            log.error("检查/创建Bucket失败", e);
            throw new RuntimeException("检查/创建Bucket失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成文件存储Key
     */
    public String generateKey(String type, String ext) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "photo/" + datePath + "/" + uuid + "_" + type + ext;
    }

    /**
     * 获取Bucket名称
     */
    public String getBucket() {
        return minIOConfig.getBucket();
    }

    /**
     * 获取文件流（服务端直接读取）
     */
    public InputStream getObject(String key) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取文件流失败: {}", key, e);
            throw new RuntimeException("获取文件流失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取公开访问URL（预览图等公开资源）
     */
    public String getPublicUrl(String key) {
        String endpoint = minIOConfig.getEndpoint();
        // 去掉末尾斜杠
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint + "/" + key;
    }

    /**
     * 获取指定版本的文件流
     */
    public InputStream getObject(String key, String versionId) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(key)
                            .versionId(versionId)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取指定版本文件流失败: {} version: {}", key, versionId, e);
            throw new RuntimeException("获取指定版本文件流失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除指定版本的文件
     */
    public void delete(String key, String versionId) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(key)
                            .versionId(versionId)
                            .build()
            );
        } catch (Exception e) {
            log.error("删除指定版本文件失败: {} version: {}", key, versionId, e);
            throw new RuntimeException("删除指定版本文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件信息（支持指定版本）
     */
    public StatObjectResponse getObjectStat(String key, String versionId) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(key)
                            .versionId(versionId)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取指定版本文件信息失败: {} version: {}", key, versionId, e);
            throw new RuntimeException("获取指定版本文件信息失败: " + e.getMessage(), e);
        }
    }
}
