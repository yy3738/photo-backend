package com.photo.module.studio.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.exception.BizException;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.module.license.entity.License;
import com.photo.module.license.mapper.LicenseMapper;
import com.photo.module.order.entity.Order;
import com.photo.module.order.entity.PointsRecord;
import com.photo.module.order.mapper.OrderMapper;
import com.photo.module.order.mapper.PointsRecordMapper;
import com.photo.module.photo.dto.PhotoRespDTO;
import com.photo.module.photo.entity.Photo;
import com.photo.module.photo.entity.PhotoTag;
import com.photo.module.photo.mapper.PhotoMapper;
import com.photo.module.photo.mapper.PhotoTagMapper;
import com.photo.module.category.entity.Category;
import com.photo.module.category.entity.Tag;
import com.photo.module.category.mapper.CategoryMapper;
import com.photo.module.category.mapper.TagMapper;
import com.photo.module.studio.dto.StudioLicenseReviewReqDTO;
import com.photo.module.studio.dto.StudioPhotoReqDTO;
import com.photo.module.user.entity.User;
import com.photo.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudioService {

    private final PhotoMapper photoMapper;
    private final PhotoTagMapper photoTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final OrderMapper orderMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final LicenseMapper licenseMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 我的作品列表
     */
    public PageResult<PhotoRespDTO> listMyPhotos(String status, PageQuery query) {
        Long userId = checkPhotographer();
        Page<Photo> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<Photo>()
                .eq(Photo::getUserId, userId)
                .eq(status != null && !status.isEmpty(), Photo::getStatus, status)
                .orderByDesc(Photo::getCreateTime);

        photoMapper.selectPage(page, wrapper);

        List<PhotoRespDTO> list = page.getRecords().stream()
                .map(this::toPhotoResp)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    /**
     * 上传作品
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPhoto(StudioPhotoReqDTO req) {
        Long userId = checkPhotographer();

        Photo photo = new Photo();
        photo.setUserId(userId);
        photo.setTitle(req.getTitle());
        photo.setDescription(req.getDescription());
        photo.setPreviewUrl(req.getPreviewUrl());
        photo.setOriginalKey(req.getOriginalKey());
        photo.setCategoryId(req.getCategoryId());
        photo.setPrice(req.getPrice());
        photo.setAllowLicense(req.getAllowLicense() != null ? req.getAllowLicense() : 0);
        photo.setStatus("pending");
        photo.setPurchaseCount(0);
        photo.setCreateBy(userId);
        photo.setUpdateBy(userId);
        photoMapper.insert(photo);

        // 保存标签关联
        savePhotoTags(photo.getId(), req.getTagIds());

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(photo.getId()));
        result.put("status", "pending");
        return result;
    }

    /**
     * 编辑作品（仅 pending/rejected 状态可编辑）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePhoto(Long photoId, StudioPhotoReqDTO req) {
        Long userId = checkPhotographer();

        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new BizException(404, "作品不存在");
        }

        if (!"pending".equals(photo.getStatus()) && !"rejected".equals(photo.getStatus())) {
            throw new BizException(400, "当前状态不允许编辑");
        }

        photo.setTitle(req.getTitle());
        photo.setDescription(req.getDescription());
        photo.setPreviewUrl(req.getPreviewUrl());
        photo.setOriginalKey(req.getOriginalKey());
        photo.setCategoryId(req.getCategoryId());
        photo.setPrice(req.getPrice());
        photo.setAllowLicense(req.getAllowLicense() != null ? req.getAllowLicense() : 0);
        photo.setStatus("pending"); // 重新提交审核
        photo.setRejectReason(null);
        photo.setUpdateBy(userId);
        photoMapper.updateById(photo);

        // 更新标签
        photoTagMapper.delete(
                new LambdaQueryWrapper<PhotoTag>().eq(PhotoTag::getPhotoId, photoId));
        savePhotoTags(photoId, req.getTagIds());

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(photo.getId()));
        result.put("status", "pending");
        return result;
    }

    /**
     * 删除作品
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long photoId) {
        Long userId = checkPhotographer();

        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new BizException(404, "作品不存在");
        }

        // 已有订单的作品不允许删除
        Long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getPhotoId, photoId));
        if (orderCount > 0) {
            throw new BizException(400, "已有用户购买，不允许删除");
        }

        photoTagMapper.delete(
                new LambdaQueryWrapper<PhotoTag>().eq(PhotoTag::getPhotoId, photoId));
        photoMapper.deleteById(photoId);
    }

    /**
     * 销售统计
     */
    public Map<String, Object> getSalesStats() {
        Long userId = checkPhotographer();

        // 总作品数
        Long totalPhotos = photoMapper.selectCount(
                new LambdaQueryWrapper<Photo>().eq(Photo::getUserId, userId));

        // 已审核通过
        Long approvedPhotos = photoMapper.selectCount(
                new LambdaQueryWrapper<Photo>()
                        .eq(Photo::getUserId, userId)
                        .eq(Photo::getStatus, "approved"));

        // 总收入（从积分流水统计）
        List<PointsRecord> earnRecords = pointsRecordMapper.selectList(
                new LambdaQueryWrapper<PointsRecord>()
                        .eq(PointsRecord::getUserId, userId)
                        .eq(PointsRecord::getType, "earn"));
        int totalEarnings = earnRecords.stream().mapToInt(PointsRecord::getAmount).sum();

        // 总销量
        List<Photo> myPhotos = photoMapper.selectList(
                new LambdaQueryWrapper<Photo>().eq(Photo::getUserId, userId));
        int totalSales = myPhotos.stream().mapToInt(Photo::getPurchaseCount).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPhotos", totalPhotos);
        stats.put("approvedPhotos", approvedPhotos);
        stats.put("totalEarnings", totalEarnings);
        stats.put("totalSales", totalSales);
        return stats;
    }

    /**
     * 收到的授权申请列表
     */
    public PageResult<Map<String, Object>> listReceivedLicenses(String status, PageQuery query) {
        Long userId = checkPhotographer();

        // 查出我的所有作品 ID
        List<Long> myPhotoIds = photoMapper.selectList(
                new LambdaQueryWrapper<Photo>().eq(Photo::getUserId, userId)
                        .select(Photo::getId)
        ).stream().map(Photo::getId).collect(Collectors.toList());

        if (myPhotoIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPage(), query.getPageSize());
        }

        Page<License> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<License> wrapper = new LambdaQueryWrapper<License>()
                .in(License::getPhotoId, myPhotoIds)
                .eq(status != null && !status.isEmpty(), License::getStatus, status)
                .orderByDesc(License::getCreateTime);

        licenseMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = page.getRecords().stream().map(l -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(l.getId()));
            map.put("photoId", String.valueOf(l.getPhotoId()));
            map.put("photoTitle", l.getPhotoTitle());
            map.put("applicantId", String.valueOf(l.getApplicantId()));
            map.put("purpose", l.getPurpose());
            map.put("scene", l.getScene());
            map.put("duration", l.getDuration());
            map.put("contact", l.getContact());
            map.put("status", l.getStatus());
            map.put("createdAt", l.getCreateTime() != null ? l.getCreateTime().format(ISO_FMT) : null);
            return map;
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    /**
     * 摄影师审批授权申请
     */
    public Map<String, Object> reviewLicense(Long licenseId, StudioLicenseReviewReqDTO req) {
        Long userId = checkPhotographer();

        License license = licenseMapper.selectById(licenseId);
        if (license == null) {
            throw new BizException(404, "授权申请不存在");
        }

        // 校验是否是自己作品的授权申请
        Photo photo = photoMapper.selectById(license.getPhotoId());
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new BizException(403, "无权审批该授权申请");
        }

        if (!"pending_photographer".equals(license.getStatus())) {
            throw new BizException(400, "当前状态不允许审批");
        }

        if ("approve".equals(req.getAction())) {
            license.setStatus("approved");
        } else if ("reject".equals(req.getAction())) {
            license.setStatus("rejected");
            license.setRejectReason(req.getRejectReason());
        } else {
            throw new BizException(400, "无效操作");
        }

        license.setUpdateBy(userId);
        licenseMapper.updateById(license);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(license.getId()));
        result.put("status", license.getStatus());
        return result;
    }

    /**
     * 收入明细
     */
    public PageResult<Map<String, Object>> getEarnings(PageQuery query) {
        Long userId = checkPhotographer();
        Page<PointsRecord> page = new Page<>(query.getPage(), query.getPageSize());

        pointsRecordMapper.selectPage(page,
                new LambdaQueryWrapper<PointsRecord>()
                        .eq(PointsRecord::getUserId, userId)
                        .eq(PointsRecord::getType, "earn")
                        .orderByDesc(PointsRecord::getCreateTime));

        List<Map<String, Object>> list = page.getRecords().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(r.getId()));
            map.put("amount", r.getAmount());
            map.put("balance", r.getBalance());
            map.put("remark", r.getRemark());
            map.put("relatedPhotoId", r.getRelatedPhotoId() != null ? String.valueOf(r.getRelatedPhotoId()) : null);
            map.put("relatedPhotoTitle", r.getRelatedPhotoTitle());
            map.put("createdAt", r.getCreateTime() != null ? r.getCreateTime().format(ISO_FMT) : null);
            return map;
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    // ========== 私有方法 ==========

    private Long checkPhotographer() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (!"photographer".equals(user.getRole()) && !"admin".equals(user.getRole())) {
            throw new BizException(403, "仅摄影师可访问工作台");
        }
        return userId;
    }

    private void savePhotoTags(Long photoId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            PhotoTag pt = new PhotoTag();
            pt.setPhotoId(photoId);
            pt.setTagId(tagId);
            photoTagMapper.insert(pt);
        }
    }

    private PhotoRespDTO toPhotoResp(Photo photo) {
        PhotoRespDTO dto = new PhotoRespDTO();
        dto.setId(String.valueOf(photo.getId()));
        dto.setTitle(photo.getTitle());
        dto.setDescription(photo.getDescription());
        dto.setPreviewUrl(photo.getPreviewUrl());
        dto.setOriginalKey(photo.getOriginalKey());
        dto.setCategoryId(String.valueOf(photo.getCategoryId()));
        dto.setPrice(photo.getPrice());
        dto.setAllowLicense(photo.getAllowLicense() == 1);
        dto.setStatus(photo.getStatus());
        dto.setRejectReason(photo.getRejectReason());
        dto.setPurchaseCount(photo.getPurchaseCount());
        dto.setCreatedAt(photo.getCreateTime() != null ? photo.getCreateTime().format(ISO_FMT) : null);
        dto.setUpdatedAt(photo.getUpdateTime() != null ? photo.getUpdateTime().format(ISO_FMT) : null);

        // 分类名称
        Category category = categoryMapper.selectById(photo.getCategoryId());
        if (category != null) {
            dto.setCategoryName(category.getName());
        }

        // 标签
        List<PhotoTag> photoTags = photoTagMapper.selectList(
                new LambdaQueryWrapper<PhotoTag>().eq(PhotoTag::getPhotoId, photo.getId()));
        List<PhotoRespDTO.TagItem> tagItems = photoTags.stream().map(pt -> {
            Tag tag = tagMapper.selectById(pt.getTagId());
            PhotoRespDTO.TagItem item = new PhotoRespDTO.TagItem();
            if (tag != null) {
                item.setId(String.valueOf(tag.getId()));
                item.setName(tag.getName());
            }
            return item;
        }).collect(Collectors.toList());
        dto.setTags(tagItems);

        return dto;
    }
}
