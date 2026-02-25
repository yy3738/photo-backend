package com.photo.mvc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.exception.BizException;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.mvc.entity.model.*;
import com.photo.mvc.entity.vo.BuyPhotoVO;
import com.photo.mvc.entity.vo.DownloadVO;
import com.photo.mvc.entity.vo.PhotoVO;
import com.photo.mvc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizPhotoService {

    private final BizPhotoMapper bizPhotoMapper;
    private final BizPhotoTagMapper bizPhotoTagMapper;
    private final SysCategoryMapper sysCategoryMapper;
    private final SysTagMapper sysTagMapper;
    private final BizOrderMapper bizOrderMapper;
    private final BizPointsRecordMapper bizPointsRecordMapper;
    private final SysUserMapper sysUserMapper;

    @Value("${oss.host:https://oss.example.com}")
    private String ossHost;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public PageResult<PhotoVO> listPhotos(Long categoryId, Long tagId, PageQuery query) {
        Page<BizPhotoPO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<BizPhotoPO> wrapper = new LambdaQueryWrapper<BizPhotoPO>()
                .eq(BizPhotoPO::getStatus, "approved")
                .eq(categoryId != null, BizPhotoPO::getCategoryId, categoryId)
                .orderByDesc(BizPhotoPO::getCreateTime);

        if (tagId != null) {
            List<Long> photoIds = bizPhotoTagMapper.selectList(
                    new LambdaQueryWrapper<BizPhotoTagPO>().eq(BizPhotoTagPO::getTagId, tagId)
            ).stream().map(BizPhotoTagPO::getPhotoId).collect(Collectors.toList());

            if (photoIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPage(), query.getPageSize());
            }
            wrapper.in(BizPhotoPO::getId, photoIds);
        }

        bizPhotoMapper.selectPage(page, wrapper);

        List<PhotoVO> list = page.getRecords().stream()
                .map(this::toPhotoVO)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public PageResult<PhotoVO> searchPhotos(String keyword, Long categoryId, Long tagId, PageQuery query) {
        Page<BizPhotoPO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<BizPhotoPO> wrapper = new LambdaQueryWrapper<BizPhotoPO>()
                .eq(BizPhotoPO::getStatus, "approved")
                .eq(categoryId != null, BizPhotoPO::getCategoryId, categoryId)
                .like(keyword != null && !keyword.isEmpty(), BizPhotoPO::getTitle, keyword)
                .orderByDesc(BizPhotoPO::getCreateTime);

        if (tagId != null) {
            List<Long> photoIds = bizPhotoTagMapper.selectList(
                    new LambdaQueryWrapper<BizPhotoTagPO>().eq(BizPhotoTagPO::getTagId, tagId)
            ).stream().map(BizPhotoTagPO::getPhotoId).collect(Collectors.toList());

            if (photoIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPage(), query.getPageSize());
            }
            wrapper.in(BizPhotoPO::getId, photoIds);
        }

        bizPhotoMapper.selectPage(page, wrapper);

        List<PhotoVO> list = page.getRecords().stream()
                .map(this::toPhotoVO)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public PhotoVO getPhotoDetail(Long id) {
        BizPhotoPO photo = bizPhotoMapper.selectById(id);
        if (photo == null || !"approved".equals(photo.getStatus())) {
            throw new BizException(404, "作品不存在");
        }

        PhotoVO vo = toPhotoVO(photo);

        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            Long orderCount = bizOrderMapper.selectCount(
                    new LambdaQueryWrapper<BizOrderPO>()
                            .eq(BizOrderPO::getUserId, userId)
                            .eq(BizOrderPO::getPhotoId, id));
            vo.setIsPurchased(orderCount > 0);
        } else {
            vo.setIsPurchased(false);
        }

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public BuyPhotoVO buyPhoto(Long photoId) {
        Long userId = StpUtil.getLoginIdAsLong();

        BizPhotoPO photo = bizPhotoMapper.selectById(photoId);
        if (photo == null || !"approved".equals(photo.getStatus())) {
            throw new BizException(404, "作品不存在或未上架");
        }

        if (photo.getUserId().equals(userId)) {
            throw new BizException(409, "不能购买自己的作品");
        }

        Long existCount = bizOrderMapper.selectCount(
                new LambdaQueryWrapper<BizOrderPO>()
                        .eq(BizOrderPO::getUserId, userId)
                        .eq(BizOrderPO::getPhotoId, photoId));
        if (existCount > 0) {
            throw new BizException(409, "已购买过该作品");
        }

        SysUserPO buyer = sysUserMapper.selectById(userId);
        if (buyer.getPoints() < photo.getPrice()) {
            throw new BizException(409, "积分不足");
        }

        int price = photo.getPrice();
        int photographerEarn = (int) (price * 0.9);

        buyer.setPoints(buyer.getPoints() - price);
        sysUserMapper.updateById(buyer);

        SysUserPO photographer = sysUserMapper.selectById(photo.getUserId());
        photographer.setPoints(photographer.getPoints() + photographerEarn);
        sysUserMapper.updateById(photographer);

        BizOrderPO order = new BizOrderPO();
        order.setUserId(userId);
        order.setPhotoId(photoId);
        order.setPhotoTitle(photo.getTitle());
        order.setPhotoPreviewUrl(photo.getPreviewUrl());
        order.setPrice(price);
        order.setCreateBy(userId);
        order.setUpdateBy(userId);
        bizOrderMapper.insert(order);

        photo.setPurchaseCount(photo.getPurchaseCount() + 1);
        bizPhotoMapper.updateById(photo);

        BizPointsRecordPO spendRecord = new BizPointsRecordPO();
        spendRecord.setUserId(userId);
        spendRecord.setType("spend");
        spendRecord.setAmount(price);
        spendRecord.setBalance(buyer.getPoints());
        spendRecord.setRemark("购买作品: " + photo.getTitle());
        spendRecord.setRelatedOrderId(order.getId());
        spendRecord.setRelatedPhotoId(photoId);
        spendRecord.setRelatedPhotoTitle(photo.getTitle());
        spendRecord.setCreateBy(userId);
        spendRecord.setUpdateBy(userId);
        bizPointsRecordMapper.insert(spendRecord);

        BizPointsRecordPO earnRecord = new BizPointsRecordPO();
        earnRecord.setUserId(photo.getUserId());
        earnRecord.setType("earn");
        earnRecord.setAmount(photographerEarn);
        earnRecord.setBalance(photographer.getPoints());
        earnRecord.setRemark("作品被购买: " + photo.getTitle());
        earnRecord.setRelatedOrderId(order.getId());
        earnRecord.setRelatedPhotoId(photoId);
        earnRecord.setRelatedPhotoTitle(photo.getTitle());
        earnRecord.setCreateBy(userId);
        earnRecord.setUpdateBy(userId);
        bizPointsRecordMapper.insert(earnRecord);

        BuyPhotoVO resp = new BuyPhotoVO();
        resp.setOrderId(String.valueOf(order.getId()));
        resp.setPointsSpent(price);
        resp.setRemainingPoints(buyer.getPoints());
        return resp;
    }

    public DownloadVO getDownloadUrl(Long photoId) {
        Long userId = StpUtil.getLoginIdAsLong();
        BizPhotoPO photo = bizPhotoMapper.selectById(photoId);

        if (photo == null) {
            throw new BizException(404, "作品不存在");
        }

        boolean isOwner = photo.getUserId().equals(userId);
        boolean isAdmin = StpUtil.hasRole("admin");
        boolean hasPurchased = bizOrderMapper.selectCount(
                new LambdaQueryWrapper<BizOrderPO>()
                        .eq(BizOrderPO::getUserId, userId)
                        .eq(BizOrderPO::getPhotoId, photoId)) > 0;

        if (!isOwner && !isAdmin && !hasPurchased) {
            throw new BizException(403, "无权下载该作品");
        }

        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(5);
        String url = ossHost + "/" + photo.getOriginalKey() + "?expire=" + expireAt.format(ISO_FMT);

        DownloadVO resp = new DownloadVO();
        resp.setUrl(url);
        resp.setExpireAt(expireAt.format(ISO_FMT));
        return resp;
    }

    private PhotoVO toPhotoVO(BizPhotoPO photo) {
        PhotoVO vo = new PhotoVO();
        vo.setId(String.valueOf(photo.getId()));
        vo.setTitle(photo.getTitle());
        vo.setDescription(photo.getDescription());
        vo.setPreviewUrl(photo.getPreviewUrl());
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

        SysUserPO photographer = sysUserMapper.selectById(photo.getUserId());
        if (photographer != null) {
            PhotoVO.PhotographerInfo info = new PhotoVO.PhotographerInfo();
            info.setId(String.valueOf(photographer.getId()));
            info.setNickname(photographer.getNickname());
            info.setAvatar(photographer.getAvatar());
            vo.setPhotographer(info);
        }

        return vo;
    }
}
