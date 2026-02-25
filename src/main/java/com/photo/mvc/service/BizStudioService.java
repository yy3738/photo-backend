package com.photo.mvc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.exception.BizException;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.mvc.entity.model.*;
import com.photo.mvc.entity.req.StudioLicenseReviewReq;
import com.photo.mvc.entity.req.StudioPhotoReq;
import com.photo.mvc.entity.req.StudioPhotoStatusReq;
import com.photo.mvc.entity.vo.PhotoVO;
import com.photo.mvc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizStudioService {

    private final BizPhotoMapper bizPhotoMapper;
    private final BizPhotoTagMapper bizPhotoTagMapper;
    private final SysCategoryMapper sysCategoryMapper;
    private final SysTagMapper sysTagMapper;
    private final BizOrderMapper bizOrderMapper;
    private final BizPointsRecordMapper bizPointsRecordMapper;
    private final BizLicenseMapper bizLicenseMapper;
    private final SysUserMapper sysUserMapper;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public PageResult<PhotoVO> listMyPhotos(String status, PageQuery query) {
        Long userId = checkPhotographer();
        Page<BizPhotoPO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<BizPhotoPO> wrapper = new LambdaQueryWrapper<BizPhotoPO>()
                .eq(BizPhotoPO::getUserId, userId)
                .eq(status != null && !status.isEmpty(), BizPhotoPO::getStatus, status)
                .orderByDesc(BizPhotoPO::getCreateTime);

        bizPhotoMapper.selectPage(page, wrapper);

        List<PhotoVO> list = page.getRecords().stream()
                .map(this::toPhotoVO)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPhoto(StudioPhotoReq req) {
        Long userId = checkPhotographer();

        BizPhotoPO photo = new BizPhotoPO();
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
        bizPhotoMapper.insert(photo);

        savePhotoTags(photo.getId(), req.getTagIds());

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(photo.getId()));
        result.put("status", "pending");
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePhoto(Long photoId, StudioPhotoReq req) {
        Long userId = checkPhotographer();

        BizPhotoPO photo = bizPhotoMapper.selectById(photoId);
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
        photo.setStatus("pending");
        photo.setRejectReason(null);
        photo.setUpdateBy(userId);
        bizPhotoMapper.updateById(photo);

        bizPhotoTagMapper.delete(
                new LambdaQueryWrapper<BizPhotoTagPO>().eq(BizPhotoTagPO::getPhotoId, photoId));
        savePhotoTags(photoId, req.getTagIds());

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(photo.getId()));
        result.put("status", "pending");
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long photoId) {
        Long userId = checkPhotographer();

        BizPhotoPO photo = bizPhotoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new BizException(404, "作品不存在");
        }

        Long orderCount = bizOrderMapper.selectCount(
                new LambdaQueryWrapper<BizOrderPO>().eq(BizOrderPO::getPhotoId, photoId));
        if (orderCount > 0) {
            throw new BizException(400, "已有用户购买，不允许删除");
        }

        bizPhotoTagMapper.delete(
                new LambdaQueryWrapper<BizPhotoTagPO>().eq(BizPhotoTagPO::getPhotoId, photoId));
        bizPhotoMapper.deleteById(photoId);
    }

    public Map<String, Object> getSalesStats() {
        Long userId = checkPhotographer();

        Long totalPhotos = bizPhotoMapper.selectCount(
                new LambdaQueryWrapper<BizPhotoPO>().eq(BizPhotoPO::getUserId, userId));

        Long approvedPhotos = bizPhotoMapper.selectCount(
                new LambdaQueryWrapper<BizPhotoPO>()
                        .eq(BizPhotoPO::getUserId, userId)
                        .eq(BizPhotoPO::getStatus, "approved"));

        List<BizPointsRecordPO> earnRecords = bizPointsRecordMapper.selectList(
                new LambdaQueryWrapper<BizPointsRecordPO>()
                        .eq(BizPointsRecordPO::getUserId, userId)
                        .eq(BizPointsRecordPO::getType, "earn"));
        int totalEarnings = earnRecords.stream().mapToInt(BizPointsRecordPO::getAmount).sum();

        List<BizPhotoPO> myPhotos = bizPhotoMapper.selectList(
                new LambdaQueryWrapper<BizPhotoPO>().eq(BizPhotoPO::getUserId, userId));
        int totalSales = myPhotos.stream().mapToInt(BizPhotoPO::getPurchaseCount).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPhotos", totalPhotos);
        stats.put("approvedPhotos", approvedPhotos);
        stats.put("totalEarnings", totalEarnings);
        stats.put("totalSales", totalSales);
        return stats;
    }

    public PageResult<Map<String, Object>> listReceivedLicenses(String status, PageQuery query) {
        Long userId = checkPhotographer();

        List<Long> myPhotoIds = bizPhotoMapper.selectList(
                new LambdaQueryWrapper<BizPhotoPO>().eq(BizPhotoPO::getUserId, userId)
                        .select(BizPhotoPO::getId)
        ).stream().map(BizPhotoPO::getId).collect(Collectors.toList());

        if (myPhotoIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPage(), query.getPageSize());
        }

        Page<BizLicensePO> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<BizLicensePO> wrapper = new LambdaQueryWrapper<BizLicensePO>()
                .in(BizLicensePO::getPhotoId, myPhotoIds)
                .eq(status != null && !status.isEmpty(), BizLicensePO::getStatus, status)
                .orderByDesc(BizLicensePO::getCreateTime);

        bizLicenseMapper.selectPage(page, wrapper);

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

    public Map<String, Object> reviewLicense(Long licenseId, StudioLicenseReviewReq req) {
        Long userId = checkPhotographer();

        BizLicensePO license = bizLicenseMapper.selectById(licenseId);
        if (license == null) {
            throw new BizException(404, "授权申请不存在");
        }

        BizPhotoPO photo = bizPhotoMapper.selectById(license.getPhotoId());
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
        bizLicenseMapper.updateById(license);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(license.getId()));
        result.put("status", license.getStatus());
        return result;
    }

    public PageResult<Map<String, Object>> getEarnings(PageQuery query) {
        Long userId = checkPhotographer();
        Page<BizPointsRecordPO> page = new Page<>(query.getPage(), query.getPageSize());

        bizPointsRecordMapper.selectPage(page,
                new LambdaQueryWrapper<BizPointsRecordPO>()
                        .eq(BizPointsRecordPO::getUserId, userId)
                        .eq(BizPointsRecordPO::getType, "earn")
                        .orderByDesc(BizPointsRecordPO::getCreateTime));

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

    public PhotoVO getMyPhotoDetail(Long photoId) {
        Long userId = checkPhotographer();
        BizPhotoPO photo = bizPhotoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new BizException(404, "作品不存在");
        }
        return toPhotoVO(photo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> togglePhotoStatus(Long photoId, StudioPhotoStatusReq req) {
        Long userId = checkPhotographer();
        BizPhotoPO photo = bizPhotoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new BizException(404, "作品不存在");
        }

        String current = photo.getStatus();
        String target = req.getStatus();

        if ("approved".equals(current) && "offline".equals(target)) {
            photo.setStatus("offline");
        } else if ("offline".equals(current) && "approved".equals(target)) {
            photo.setStatus("approved");
        } else {
            throw new BizException(400, "当前状态不允许此操作");
        }

        photo.setUpdateBy(userId);
        bizPhotoMapper.updateById(photo);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(photo.getId()));
        result.put("status", photo.getStatus());
        return result;
    }

    private Long checkPhotographer() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserPO user = sysUserMapper.selectById(userId);
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
            BizPhotoTagPO pt = new BizPhotoTagPO();
            pt.setPhotoId(photoId);
            pt.setTagId(tagId);
            bizPhotoTagMapper.insert(pt);
        }
    }

    private PhotoVO toPhotoVO(BizPhotoPO photo) {
        PhotoVO vo = new PhotoVO();
        vo.setId(String.valueOf(photo.getId()));
        vo.setTitle(photo.getTitle());
        vo.setDescription(photo.getDescription());
        vo.setPreviewUrl(photo.getPreviewUrl());
        vo.setOriginalKey(photo.getOriginalKey());
        vo.setCategoryId(String.valueOf(photo.getCategoryId()));
        vo.setPrice(photo.getPrice());
        vo.setAllowLicense(photo.getAllowLicense() == 1);
        vo.setStatus(photo.getStatus());
        vo.setRejectReason(photo.getRejectReason());
        vo.setPurchaseCount(photo.getPurchaseCount());
        vo.setCreatedAt(photo.getCreateTime() != null ? photo.getCreateTime().format(ISO_FMT) : null);
        vo.setUpdatedAt(photo.getUpdateTime() != null ? photo.getUpdateTime().format(ISO_FMT) : null);

        SysCategoryPO category = sysCategoryMapper.selectById(photo.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        List<BizPhotoTagPO> photoTags = bizPhotoTagMapper.selectList(
                new LambdaQueryWrapper<BizPhotoTagPO>().eq(BizPhotoTagPO::getPhotoId, photo.getId()));
        List<PhotoVO.TagItem> tagItems = photoTags.stream().map(pt -> {
            SysTagPO tag = sysTagMapper.selectById(pt.getTagId());
            PhotoVO.TagItem item = new PhotoVO.TagItem();
            if (tag != null) {
                item.setId(String.valueOf(tag.getId()));
                item.setName(tag.getName());
            }
            return item;
        }).collect(Collectors.toList());
        vo.setTags(tagItems);

        return vo;
    }
}
