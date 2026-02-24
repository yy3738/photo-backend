package com.photo.module.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.exception.BizException;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.module.admin.dto.*;
import com.photo.module.category.entity.Category;
import com.photo.module.category.entity.Tag;
import com.photo.module.category.mapper.CategoryMapper;
import com.photo.module.category.mapper.TagMapper;
import com.photo.module.license.entity.License;
import com.photo.module.license.mapper.LicenseMapper;
import com.photo.module.order.entity.PointsRecord;
import com.photo.module.order.mapper.OrderMapper;
import com.photo.module.order.mapper.PointsRecordMapper;
import com.photo.module.photo.entity.Photo;
import com.photo.module.photo.mapper.PhotoMapper;
import com.photo.module.user.entity.User;
import com.photo.module.user.mapper.UserMapper;
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

    private final UserMapper userMapper;
    private final PhotoMapper photoMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final OrderMapper orderMapper;
    private final LicenseMapper licenseMapper;
    private final PointsRecordMapper pointsRecordMapper;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ========== 仪表盘 ==========

    public Map<String, Object> getDashboard() {
        checkAdmin();
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalPhotos", photoMapper.selectCount(null));
        data.put("pendingPhotos", photoMapper.selectCount(
                new LambdaQueryWrapper<Photo>().eq(Photo::getStatus, "pending")));
        data.put("totalOrders", orderMapper.selectCount(null));
        data.put("pendingLicenses", licenseMapper.selectCount(
                new LambdaQueryWrapper<License>().eq(License::getStatus, "pending_admin")));
        return data;
    }

    // ========== 用户管理 ==========

    public PageResult<Map<String, Object>> listUsers(String role, PageQuery query) {
        checkAdmin();
        Page<User> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(role != null && !role.isEmpty(), User::getRole, role)
                .orderByDesc(User::getCreateTime);

        userMapper.selectPage(page, wrapper);

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
        User user = userMapper.selectById(userId);
        if (user == null) throw new BizException(404, "用户不存在");
        user.setIsBanned(1);
        userMapper.updateById(user);
    }

    public void unbanUser(Long userId) {
        checkAdmin();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BizException(404, "用户不存在");
        user.setIsBanned(0);
        userMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rechargePoints(AdminRechargeReqDTO req) {
        Long adminId = checkAdmin();

        User user = userMapper.selectById(req.getUserId());
        if (user == null) throw new BizException(404, "用户不存在");

        user.setPoints(user.getPoints() + req.getAmount());
        userMapper.updateById(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(req.getUserId());
        record.setType("recharge");
        record.setAmount(req.getAmount());
        record.setBalance(user.getPoints());
        record.setRemark(req.getRemark());
        record.setCreateBy(adminId);
        record.setUpdateBy(adminId);
        pointsRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", String.valueOf(user.getId()));
        result.put("points", user.getPoints());
        return result;
    }

    // ========== 作品审核 ==========

    public PageResult<Map<String, Object>> listPhotosForReview(String status, PageQuery query) {
        checkAdmin();
        Page<Photo> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<Photo>()
                .eq(status != null && !status.isEmpty(), Photo::getStatus, status)
                .orderByAsc(Photo::getCreateTime);

        photoMapper.selectPage(page, wrapper);

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

            // 摄影师信息
            User photographer = userMapper.selectById(p.getUserId());
            if (photographer != null) {
                map.put("photographerNickname", photographer.getNickname());
            }
            return map;
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public Map<String, Object> reviewPhoto(Long photoId, AdminPhotoReviewReqDTO req) {
        Long adminId = checkAdmin();

        Photo photo = photoMapper.selectById(photoId);
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
        photoMapper.updateById(photo);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(photo.getId()));
        result.put("status", photo.getStatus());
        return result;
    }

    // ========== 授权审核（管理员初审） ==========

    public PageResult<Map<String, Object>> listLicensesForReview(String status, PageQuery query) {
        checkAdmin();
        Page<License> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<License> wrapper = new LambdaQueryWrapper<License>()
                .eq(status != null && !status.isEmpty(), License::getStatus, status)
                .orderByAsc(License::getCreateTime);

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
            map.put("rejectReason", l.getRejectReason());
            map.put("createdAt", l.getCreateTime() != null ? l.getCreateTime().format(ISO_FMT) : null);
            return map;
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public Map<String, Object> reviewLicense(Long licenseId, AdminLicenseReviewReqDTO req) {
        Long adminId = checkAdmin();

        License license = licenseMapper.selectById(licenseId);
        if (license == null) throw new BizException(404, "授权申请不存在");
        if (!"pending_admin".equals(license.getStatus())) {
            throw new BizException(400, "当前状态不允许审核");
        }

        if ("approve".equals(req.getAction())) {
            license.setStatus("pending_photographer"); // 管理员通过后转摄影师审批
        } else if ("reject".equals(req.getAction())) {
            license.setStatus("rejected");
            license.setRejectReason(req.getRejectReason());
        } else {
            throw new BizException(400, "无效操作");
        }

        license.setUpdateBy(adminId);
        licenseMapper.updateById(license);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(license.getId()));
        result.put("status", license.getStatus());
        return result;
    }

    // ========== 分类管理 ==========

    public Map<String, Object> createCategory(AdminCategoryReqDTO req) {
        Long adminId = checkAdmin();
        Category category = new Category();
        category.setName(req.getName());
        category.setSort(req.getSort() != null ? req.getSort() : 0);
        category.setCreateBy(adminId);
        category.setUpdateBy(adminId);
        categoryMapper.insert(category);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(category.getId()));
        result.put("name", category.getName());
        return result;
    }

    public Map<String, Object> updateCategory(Long id, AdminCategoryReqDTO req) {
        Long adminId = checkAdmin();
        Category category = categoryMapper.selectById(id);
        if (category == null) throw new BizException(404, "分类不存在");

        category.setName(req.getName());
        if (req.getSort() != null) category.setSort(req.getSort());
        category.setUpdateBy(adminId);
        categoryMapper.updateById(category);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(category.getId()));
        result.put("name", category.getName());
        return result;
    }

    public void deleteCategory(Long id) {
        checkAdmin();
        // 检查是否有关联作品
        Long count = photoMapper.selectCount(
                new LambdaQueryWrapper<Photo>().eq(Photo::getCategoryId, id));
        if (count > 0) {
            throw new BizException(400, "该分类下有作品，不允许删除");
        }
        // 删除关联标签
        tagMapper.delete(new LambdaQueryWrapper<Tag>().eq(Tag::getCategoryId, id));
        categoryMapper.deleteById(id);
    }

    // ========== 标签管理 ==========

    public Map<String, Object> createTag(AdminTagReqDTO req) {
        Long adminId = checkAdmin();
        Tag tag = new Tag();
        tag.setName(req.getName());
        tag.setCategoryId(req.getCategoryId());
        tag.setSort(req.getSort() != null ? req.getSort() : 0);
        tag.setCreateBy(adminId);
        tag.setUpdateBy(adminId);
        tagMapper.insert(tag);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(tag.getId()));
        result.put("name", tag.getName());
        return result;
    }

    public Map<String, Object> updateTag(Long id, AdminTagReqDTO req) {
        Long adminId = checkAdmin();
        Tag tag = tagMapper.selectById(id);
        if (tag == null) throw new BizException(404, "标签不存在");

        tag.setName(req.getName());
        tag.setCategoryId(req.getCategoryId());
        if (req.getSort() != null) tag.setSort(req.getSort());
        tag.setUpdateBy(adminId);
        tagMapper.updateById(tag);

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(tag.getId()));
        result.put("name", tag.getName());
        return result;
    }

    public void deleteTag(Long id) {
        checkAdmin();
        tagMapper.deleteById(id);
    }

    // ========== 私有方法 ==========

    private Long checkAdmin() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (!"admin".equals(user.getRole())) {
            throw new BizException(403, "仅管理员可访问");
        }
        return userId;
    }
}
