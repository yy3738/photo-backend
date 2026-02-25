package com.photo.mvc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.exception.BizException;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.mvc.entity.model.*;
import com.photo.mvc.entity.req.*;
import com.photo.mvc.entity.vo.CategoryVO;
import com.photo.mvc.entity.vo.TagVO;
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
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Map<String, Object> getDashboard() {
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
        Page<SysUserPO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<SysUserPO> wrapper = new LambdaQueryWrapper<SysUserPO>()
                .orderByDesc(SysUserPO::getCreateTime);

        // 按角色筛选：通过关系表查 userIds
        if (role != null && !role.isEmpty()) {
            SysRolePO sysRole = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRolePO>().eq(SysRolePO::getCode, role));
            if (sysRole == null) {
                return PageResult.of(List.of(), 0L, query.getPage(), query.getPageSize());
            }
            List<Long> userIds = sysUserRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRolePO>().eq(SysUserRolePO::getRoleId, sysRole.getId())
            ).stream().map(SysUserRolePO::getUserId).collect(Collectors.toList());
            if (userIds.isEmpty()) {
                return PageResult.of(List.of(), 0L, query.getPage(), query.getPageSize());
            }
            wrapper.in(SysUserPO::getId, userIds);
        }

        sysUserMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = page.getRecords().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(u.getId()));
            map.put("openid", u.getOpenid());
            map.put("nickname", u.getNickname());
            map.put("avatar", u.getAvatar());
            map.put("roles", getUserRoleCodes(u.getId()));
            map.put("points", u.getPoints());
            map.put("isBanned", u.getIsBanned() == 1);
            map.put("createdAt", u.getCreateTime() != null ? u.getCreateTime().format(ISO_FMT) : null);
            return map;
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public void banUser(Long userId) {
        SysUserPO user = sysUserMapper.selectById(userId);
        if (user == null) throw new BizException(404, "用户不存在");
        user.setIsBanned(1);
        sysUserMapper.updateById(user);
    }

    public void unbanUser(Long userId) {
        SysUserPO user = sysUserMapper.selectById(userId);
        if (user == null) throw new BizException(404, "用户不存在");
        user.setIsBanned(0);
        sysUserMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rechargePoints(AdminRechargeReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();

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
        Long adminId = StpUtil.getLoginIdAsLong();

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
        Long adminId = StpUtil.getLoginIdAsLong();

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
        Long adminId = StpUtil.getLoginIdAsLong();
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
        Long adminId = StpUtil.getLoginIdAsLong();
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
        Long count = bizPhotoMapper.selectCount(
                new LambdaQueryWrapper<BizPhotoPO>().eq(BizPhotoPO::getCategoryId, id));
        if (count > 0) {
            throw new BizException(400, "该分类下有作品，不允许删除");
        }
        sysTagMapper.delete(new LambdaQueryWrapper<SysTagPO>().eq(SysTagPO::getCategoryId, id));
        sysCategoryMapper.deleteById(id);
    }

    public Map<String, Object> createTag(AdminTagReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();
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
        Long adminId = StpUtil.getLoginIdAsLong();
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
        sysTagMapper.deleteById(id);
    }

    private List<String> getUserRoleCodes(Long userId) {
        List<SysUserRolePO> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRolePO>().eq(SysUserRolePO::getUserId, userId));
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRolePO::getRoleId).collect(Collectors.toList());
        List<SysRolePO> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRolePO>()
                        .in(SysRolePO::getId, roleIds)
                        .eq(SysRolePO::getStatus, 1));
        return roles.stream().map(SysRolePO::getCode).collect(Collectors.toList());
    }

    public List<CategoryVO> listCategories() {
        List<SysCategoryPO> categories = sysCategoryMapper.selectList(
                new LambdaQueryWrapper<SysCategoryPO>().orderByAsc(SysCategoryPO::getSort));
        return categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(String.valueOf(c.getId()));
            vo.setName(c.getName());
            vo.setSort(c.getSort());
            vo.setPhotoCount(c.getPhotoCount() != null ? c.getPhotoCount().longValue() : 0L);
            return vo;
        }).collect(Collectors.toList());
    }

    public List<TagVO> listTags(Long categoryId) {
        LambdaQueryWrapper<SysTagPO> wrapper = new LambdaQueryWrapper<SysTagPO>()
                .eq(categoryId != null, SysTagPO::getCategoryId, categoryId)
                .orderByAsc(SysTagPO::getSort);
        List<SysTagPO> tags = sysTagMapper.selectList(wrapper);
        return tags.stream().map(t -> {
            TagVO vo = new TagVO();
            vo.setId(String.valueOf(t.getId()));
            vo.setName(t.getName());
            vo.setCategoryId(String.valueOf(t.getCategoryId()));
            vo.setSort(t.getSort());
            return vo;
        }).collect(Collectors.toList());
    }

}
