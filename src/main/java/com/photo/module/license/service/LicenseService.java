package com.photo.module.license.service;

import cn.dev33.satoken.stp.StpUtil;
import com.photo.common.exception.BizException;
import com.photo.module.license.dto.LicenseCreateReqDTO;
import com.photo.module.license.entity.License;
import com.photo.module.license.mapper.LicenseMapper;
import com.photo.module.photo.entity.Photo;
import com.photo.module.photo.mapper.PhotoMapper;
import com.photo.module.user.entity.User;
import com.photo.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseMapper licenseMapper;
    private final PhotoMapper photoMapper;
    private final UserMapper userMapper;

    @Value("${oss.host:https://oss.example.com}")
    private String ossHost;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 提交授权申请
     */
    public Map<String, Object> createLicense(LicenseCreateReqDTO req) {
        Long userId = StpUtil.getLoginIdAsLong();

        Photo photo = photoMapper.selectById(req.getPhotoId());
        if (photo == null || !"approved".equals(photo.getStatus())) {
            throw new BizException(404, "作品不存在或未上架");
        }

        if (photo.getAllowLicense() != 1) {
            throw new BizException(400, "该作品不支持授权申请");
        }

        License license = new License();
        license.setPhotoId(req.getPhotoId());
        license.setPhotoTitle(photo.getTitle());
        license.setPhotoPreviewUrl(photo.getPreviewUrl());
        license.setApplicantId(userId);
        license.setPurpose(req.getPurpose());
        license.setScene(req.getScene());
        license.setDuration(req.getDuration());
        license.setContact(req.getContact());
        license.setStatus("pending_admin");
        license.setCreateBy(userId);
        license.setUpdateBy(userId);
        licenseMapper.insert(license);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(license.getId()));
        result.put("status", "pending_admin");
        return result;
    }

    /**
     * 获取授权申请详情
     */
    public Map<String, Object> getLicenseDetail(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        License license = licenseMapper.selectById(id);

        if (license == null) {
            throw new BizException(404, "授权申请不存在");
        }

        // 权限校验：申请人 / 相关摄影师 / 管理员
        Photo photo = photoMapper.selectById(license.getPhotoId());
        boolean isApplicant = license.getApplicantId().equals(userId);
        boolean isPhotographer = photo != null && photo.getUserId().equals(userId);
        boolean isAdmin = "admin".equals(user.getRole());

        if (!isApplicant && !isPhotographer && !isAdmin) {
            throw new BizException(403, "无权查看该授权申请");
        }

        return toLicenseMap(license);
    }

    /**
     * 获取授权证书下载链接
     */
    public Map<String, Object> getCertificateUrl(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        License license = licenseMapper.selectById(id);

        if (license == null) {
            throw new BizException(404, "授权申请不存在");
        }

        if (!license.getApplicantId().equals(userId)) {
            throw new BizException(403, "仅申请人可下载证书");
        }

        if (!"approved".equals(license.getStatus())) {
            throw new BizException(400, "授权申请尚未通过");
        }

        if (license.getCertificateUrl() == null || license.getCertificateUrl().isEmpty()) {
            throw new BizException(400, "证书尚未生成");
        }

        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(5);
        String url = ossHost + "/" + license.getCertificateUrl() + "?expire=" + expireAt.format(ISO_FMT);

        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("expireAt", expireAt.format(ISO_FMT));
        return result;
    }

    private Map<String, Object> toLicenseMap(License l) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(l.getId()));
        map.put("photoId", String.valueOf(l.getPhotoId()));
        map.put("photoTitle", l.getPhotoTitle());
        map.put("photoPreviewUrl", l.getPhotoPreviewUrl());
        map.put("applicantId", String.valueOf(l.getApplicantId()));
        map.put("purpose", l.getPurpose());
        map.put("scene", l.getScene());
        map.put("duration", l.getDuration());
        map.put("contact", l.getContact());
        map.put("status", l.getStatus());
        map.put("rejectReason", l.getRejectReason());
        map.put("certificateUrl", l.getCertificateUrl());
        map.put("createdAt", l.getCreateTime() != null ? l.getCreateTime().format(ISO_FMT) : null);
        map.put("updatedAt", l.getUpdateTime() != null ? l.getUpdateTime().format(ISO_FMT) : null);
        return map;
    }
}
