package com.photo.mvc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.exception.BizException;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.mvc.entity.model.*;
import com.photo.mvc.entity.vo.UserVO;
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
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final BizOrderMapper bizOrderMapper;
    private final BizPointsRecordMapper bizPointsRecordMapper;
    private final BizLicenseMapper bizLicenseMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public UserVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserPO user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toUserVO(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> enablePhotographer() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserPO user = sysUserMapper.selectById(userId);

        // 检查用户是否已拥有 photographer 角色
        SysRolePO photographerRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRolePO>()
                        .eq(SysRolePO::getCode, "photographer")
                        .eq(SysRolePO::getStatus, 1));
        if (photographerRole == null) {
            throw new BizException(500, "摄影师角色未配置");
        }

        Long exists = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRolePO>()
                        .eq(SysUserRolePO::getUserId, userId)
                        .eq(SysUserRolePO::getRoleId, photographerRole.getId()));
        if (exists > 0) {
            throw new BizException(400, "您已拥有摄影师角色");
        }

        // 插入 user_role 关联
        SysUserRolePO ur = new SysUserRolePO();
        ur.setUserId(userId);
        ur.setRoleId(photographerRole.getId());
        sysUserRoleMapper.insert(ur);

        Map<String, Object> result = new HashMap<>();
        result.put("roles", getUserRoleCodes(userId));
        return result;
    }

    public PageResult<Map<String, Object>> getPointsHistory(PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<BizPointsRecordPO> page = new Page<>(query.getPage(), query.getPageSize());

        bizPointsRecordMapper.selectPage(page,
                new LambdaQueryWrapper<BizPointsRecordPO>()
                        .eq(BizPointsRecordPO::getUserId, userId)
                        .orderByDesc(BizPointsRecordPO::getCreateTime));

        List<Map<String, Object>> list = page.getRecords().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(r.getId()));
            map.put("type", r.getType());
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

    public PageResult<Map<String, Object>> getOrders(PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<BizOrderPO> page = new Page<>(query.getPage(), query.getPageSize());

        bizOrderMapper.selectPage(page,
                new LambdaQueryWrapper<BizOrderPO>()
                        .eq(BizOrderPO::getUserId, userId)
                        .orderByDesc(BizOrderPO::getCreateTime));

        List<Map<String, Object>> list = page.getRecords().stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(o.getId()));
            map.put("photoId", String.valueOf(o.getPhotoId()));
            map.put("photoTitle", o.getPhotoTitle());
            map.put("photoPreviewUrl", o.getPhotoPreviewUrl());
            map.put("price", o.getPrice());
            map.put("createdAt", o.getCreateTime() != null ? o.getCreateTime().format(ISO_FMT) : null);
            return map;
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public PageResult<Map<String, Object>> getLicenses(String status, PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<BizLicensePO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<BizLicensePO> wrapper = new LambdaQueryWrapper<BizLicensePO>()
                .eq(BizLicensePO::getApplicantId, userId)
                .eq(status != null && !status.isEmpty(), BizLicensePO::getStatus, status)
                .orderByDesc(BizLicensePO::getCreateTime);

        bizLicenseMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = page.getRecords().stream().map(l -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(l.getId()));
            map.put("photoId", String.valueOf(l.getPhotoId()));
            map.put("photoTitle", l.getPhotoTitle());
            map.put("photoPreviewUrl", l.getPhotoPreviewUrl());
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
        }).collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    private UserVO toUserVO(SysUserPO user) {
        UserVO vo = new UserVO();
        vo.setId(String.valueOf(user.getId()));
        vo.setOpenid(user.getOpenid());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(getUserRoleCodes(user.getId()));
        vo.setPoints(user.getPoints());
        vo.setIsBanned(user.getIsBanned() == 1);
        vo.setCreatedAt(user.getCreateTime() != null ? user.getCreateTime().format(ISO_FMT) : null);
        return vo;
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
}
