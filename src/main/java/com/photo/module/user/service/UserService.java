package com.photo.module.user.service;

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
import com.photo.module.user.dto.UserRespDTO;
import com.photo.module.user.entity.User;
import com.photo.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final LicenseMapper licenseMapper;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 获取当前用户信息
     */
    public UserRespDTO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toRespDTO(user);
    }

    /**
     * 开启摄影师模式
     */
    public Map<String, String> enablePhotographer() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);

        if (!"buyer".equals(user.getRole())) {
            throw new BizException(400, "仅买家可开启摄影师模式");
        }

        user.setRole("photographer");
        userMapper.updateById(user);

        Map<String, String> result = new HashMap<>();
        result.put("role", "photographer");
        return result;
    }

    /**
     * 积分明细
     */
    public PageResult<Map<String, Object>> getPointsHistory(PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<PointsRecord> page = new Page<>(query.getPage(), query.getPageSize());

        pointsRecordMapper.selectPage(page,
                new LambdaQueryWrapper<PointsRecord>()
                        .eq(PointsRecord::getUserId, userId)
                        .orderByDesc(PointsRecord::getCreateTime));

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

    /**
     * 我的订单
     */
    public PageResult<Map<String, Object>> getOrders(PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<Order> page = new Page<>(query.getPage(), query.getPageSize());

        orderMapper.selectPage(page,
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime));

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

    /**
     * 我的授权申请
     */
    public PageResult<Map<String, Object>> getLicenses(String status, PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<License> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<License> wrapper = new LambdaQueryWrapper<License>()
                .eq(License::getApplicantId, userId)
                .eq(status != null && !status.isEmpty(), License::getStatus, status)
                .orderByDesc(License::getCreateTime);

        licenseMapper.selectPage(page, wrapper);

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

    private UserRespDTO toRespDTO(User user) {
        UserRespDTO dto = new UserRespDTO();
        dto.setId(String.valueOf(user.getId()));
        dto.setOpenid(user.getOpenid());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole());
        dto.setPoints(user.getPoints());
        dto.setIsBanned(user.getIsBanned() == 1);
        dto.setCreatedAt(user.getCreateTime() != null ? user.getCreateTime().format(ISO_FMT) : null);
        return dto;
    }
}
