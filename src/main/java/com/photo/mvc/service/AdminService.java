package com.photo.mvc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.exception.BizException;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.mvc.entity.model.*;
import com.photo.mvc.entity.req.*;
import com.photo.mvc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SysUserMapper sysUserMapper;
    private final BizPhotoMapper bizPhotoMapper;
    private final SysCategoryMapper sysCategoryMapper;
    private final SysTagMapper sysTagMapper;
    private final BizOrderMapper bizOrderMapper;
    private final BizLicenseMapper bizLicenseMapper;
    private final BizPointsRecordMapper bizPointsRecordMapper;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Map<String, Object> getDashboard() {
        checkAdmin();
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", sysUserMapper.selectCount(null));
        data.put("totalPhotos", bizPhotoMapper.selectCount(null));
        data.put("pendingPhotos", bizPhotoMapper.selectCount(
                new LambdaQueryWrapper<BizPhotoPO>().eq(BizPhotoPO::getStatus, "pending")));
        data.put("totalOrders", bizOrderMapper.selectCount(null));
        data.put("pendingLicenses", bizLicenseMapper.selectCount(
                new LambdaQueryWrapper<BizLicensePO>().eq(BizLicensePO::getStatus, "pending_admin")));
        return data;
    }

    public PageResult<Map<String, Object>> listUsers(String role, PageQuery query) {
        checkAdmin();
        Page<SysUserPO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<SysUserPO> wrapper = new LambdaQueryWrapper<SysUserPO>()
                .eq(role != null && !role.isEmpty(), SysUserPO::getRole, role)
                .orderByDesc(SysUserPO::getCreateTime);

        sysUserMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = page.getRecords().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(u.getId()));
            map.put("openid", u.getOpenid());
            map.put("nickname", u.getNickname());
            map.put("avatar", u.getAvatar());
            map.put("role", u.getRole());
            map.put("points", u.getPoints());
            map.put("isBanned", u.getIsBanned() == 1);
            map.put("createdAt", u.getCreateTime() != null ? u.getCreateTime().format(ISO_FMT) : null);
            return map;
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public void banUser(Long userId) {
        checkAdmin();
        SysUserPO user = sysUserMapper.selectById(userId);
        if (user == null) throw new BizException(404, "用户不存在");
        user.setIsBanned(1);
        sysUserMapper.updateById(user);
    }

    public void unbanUser(Long userId) {
        checkAdmin();
        SysUserPO user = sysUserMapper.selectById(userId);
        if (user == null) throw new BizException(404, "用户不存在");
        user.setIsBanned(0);
        sysUserMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rechargePoints(AdminRechargeReq req) {
        Long adminId = checkAdmin();

        SysUserPO user = sysUserMapper.selectById(req.getUserId());
        if (user == null) throw new BizException(404, "用户不存在");

        user.setPoints(user.getPoints() + req.getAmount());
        sysUserMapper.updateById(user);

        BizPointsRecordPO record = new BizPointsRecordPO();
        record.setUserId(req.getUserId());
        record.setType("recharge");
        record.setAmount(req.getAmount());
        record.setBalance(user.getPoints());
        record.setRemark(req.getRemark());
        record.setCreateBy(adminId);
        record.setUpdateBy(adminId);
        bizPointsRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", String.valueOf(user.getId()));
        result.put("points", user.getPoints());
        return result;
    }

    public PageResult<Map<String, Object>> listPhotosForReview(String status, PageQuery query) {
        checkAdmin();
        Page<BizPhotoPO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<BizPhotoPO> wrapper = new LambdaQueryWrapper<BizPhotoPO>()
                .eq(status != null && !status.isEmpty(), BizPhotoPO::getStatus, status)
                .orderByAsc(BizPhotoPO::getCreateTime);

        bizPhotoMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = page.getRecords().stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(p.getId()));
            map.put("title", p.getTitle());
            map.put("previewUrl", p.getPreviewUrl());
            map.put("categoryId", String.valueOf(p.getCategoryId()));
            map.put("userId", String.valueOf(p.getUserId()));
            map.put("price", p.getPrice());
            map.put("status", p.getStatus());
            map.put("rejectReason", p.getRejectReason());
            map.put("createdAt", p.getCreateTime() != null ? p.getCreateTime().format(ISO_FMT) : null);

            SysUserPO photographer = sysUserMapper.selectById(p.getUserId());
            if (photographer != null) {
                map.put("photographerNickname", photographer.getNickname());
            }
            return map;
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public Map<String, Object> reviewPhoto(Long photoId, AdminPhotoReviewReq req) {
        Long adminId = checkAdmin();

        BizPhotoPO photo = bizPhotoMapper.selectById(photoId);
        if (photo == null) throw new BizException(404, "作品不存在");
        if (!"pending".equals(photo.getStatus())) {
            throw new BizException(400, "当前状态不允许审核");
        }

        if ("approve".equals(req.getAction())) {
            photo.setStatus("approved");
        } else if ("reject".equals(req.getAction())) {
            photo.setStatus("rejected");
            photo.setRejectReason(req.getRejectReason());
        } else {
            throw new BizException(400, "无效操作");
        }

        photo.setUpdateBy(adminId);
        bizPhotoMapper.updateById(photo);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(photo.getId()));
        result.put("status", photo.getStatus());
        return result;
    }

    public PageResult<Map<String, Object>> listLicensesForReview(String status, PageQuery query) {
        checkAdmin();
        Page<BizLicensePO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<BizLicensePO> wrapper = new LambdaQueryWrapper<BizLicensePO>()
                .eq(status != null && !status.isEmpty(), BizLicensePO::getStatus, status)
                .orderByAsc(BizLicensePO::getCreateTime);

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
            map.put("rejectReason", l.getRejectReason());
            map.put("createdAt", l.getCreateTime() != null ? l.getCreateTime().format(ISO_FMT) : null);
            return map;
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public Map<String, Object> reviewLicense(Long licenseId, AdminLicenseReviewReq req) {
        Long adminId = checkAdmin();

        BizLicensePO license = bizLicenseMapper.selectById(licenseId);
        if (license == null) throw new BizException(404, "授权申请不存在");
        if (!"pending_admin".equals(license.getStatus())) {
            throw new BizException(400, "当前状态不允许审核");
        }

        if ("approve".equals(req.getAction())) {
            license.setStatus("pending_photographer");
        } else if ("reject".equals(req.getAction())) {
            license.setStatus("rejected");
            license.setRejectReason(req.getRejectReason());
        } else {
            throw new BizException(400, "无效操作");
        }

        license.setUpdateBy(adminId);
        bizLicenseMapper.updateById(license);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(license.getId()));
        result.put("status", license.getStatus());
        return result;
    }

    public Map<String, Object> createCategory(AdminCategoryReq req) {
        Long adminId = checkAdmin();
        SysCategoryPO category = new SysCategoryPO();
        category.setName(req.getName());
        category.setSort(req.getSort() != null ? req.getSort() : 0);
        category.setCreateBy(adminId);
        category.setUpdateBy(adminId);
        sysCategoryMapper.insert(category);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(category.getId()));
        result.put("name", category.getName());
        return result;
    }

    public Map<String, Object> updateCategory(Long id, AdminCategoryReq req) {
        Long adminId = checkAdmin();
        SysCategoryPO category = sysCategoryMapper.selectById(id);
        if (category == null) throw new BizException(404, "分类不存在");

        category.setName(req.getName());
        if (req.getSort() != null) category.setSort(req.getSort());
        category.setUpdateBy(adminId);
        sysCategoryMapper.updateById(category);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(category.getId()));
        result.put("name", category.getName());
        return result;
    }

    public void deleteCategory(Long id) {
        checkAdmin();
        Long count = bizPhotoMapper.selectCount(
                new LambdaQueryWrapper<BizPhotoPO>().eq(BizPhotoPO::getCategoryId, id));
        if (count > 0) {
            throw new BizException(400, "该分类下有作品，不允许删除");
        }
        sysTagMapper.delete(new LambdaQueryWrapper<SysTagPO>().eq(SysTagPO::getCategoryId, id));
        sysCategoryMapper.deleteById(id);
    }

    public Map<String, Object> createTag(AdminTagReq req) {
        Long adminId = checkAdmin();
        SysTagPO tag = new SysTagPO();
        tag.setName(req.getName());
        tag.setCategoryId(req.getCategoryId());
        tag.setSort(req.getSort() != null ? req.getSort() : 0);
        tag.setCreateBy(adminId);
        tag.setUpdateBy(adminId);
        sysTagMapper.insert(tag);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(tag.getId()));
        result.put("name", tag.getName());
        return result;
    }

    public Map<String, Object> updateTag(Long id, AdminTagReq req) {
        Long adminId = checkAdmin();
        SysTagPO tag = sysTagMapper.selectById(id);
        if (tag == null) throw new BizException(404, "标签不存在");

        tag.setName(req.getName());
        tag.setCategoryId(req.getCategoryId());
        if (req.getSort() != null) tag.setSort(req.getSort());
        tag.setUpdateBy(adminId);
        sysTagMapper.updateById(tag);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(tag.getId()));
        result.put("name", tag.getName());
        return result;
    }

    public void deleteTag(Long id) {
        checkAdmin();
        sysTagMapper.deleteById(id);
    }

    private Long checkAdmin() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserPO user = sysUserMapper.selectById(userId);
        if (!"admin".equals(user.getRole())) {
            throw new BizException(403, "仅管理员可访问");
        }
        return userId;
    }
}
