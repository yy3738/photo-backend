package com.photo.module.photo.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.exception.BizException;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.module.category.entity.Category;
import com.photo.module.category.entity.Tag;
import com.photo.module.category.mapper.CategoryMapper;
import com.photo.module.category.mapper.TagMapper;
import com.photo.module.order.entity.Order;
import com.photo.module.order.entity.PointsRecord;
import com.photo.module.order.mapper.OrderMapper;
import com.photo.module.order.mapper.PointsRecordMapper;
import com.photo.module.photo.dto.BuyPhotoRespDTO;
import com.photo.module.photo.dto.DownloadRespDTO;
import com.photo.module.photo.dto.PhotoRespDTO;
import com.photo.module.photo.entity.Photo;
import com.photo.module.photo.entity.PhotoTag;
import com.photo.module.photo.mapper.PhotoMapper;
import com.photo.module.photo.mapper.PhotoTagMapper;
import com.photo.module.user.entity.User;
import com.photo.module.user.mapper.UserMapper;
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
public class PhotoService {

    private final PhotoMapper photoMapper;
    private final PhotoTagMapper photoTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final OrderMapper orderMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final UserMapper userMapper;

    @Value("${oss.host:https://oss.example.com}")
    private String ossHost;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 公开作品列表（仅 approved）
     */
    public PageResult<PhotoRespDTO> listPhotos(Long categoryId, Long tagId, PageQuery query) {
        Page<Photo> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<Photo>()
                .eq(Photo::getStatus, "approved")
                .eq(categoryId != null, Photo::getCategoryId, categoryId)
                .orderByDesc(Photo::getCreateTime);

        // 如果有 tagId 筛选，先查出关联的 photoId
        if (tagId != null) {
            List<Long> photoIds = photoTagMapper.selectList(
                    new LambdaQueryWrapper<PhotoTag>().eq(PhotoTag::getTagId, tagId)
            ).stream().map(PhotoTag::getPhotoId).collect(Collectors.toList());

            if (photoIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPage(), query.getPageSize());
            }
            wrapper.in(Photo::getId, photoIds);
        }

        photoMapper.selectPage(page, wrapper);

        List<PhotoRespDTO> list = page.getRecords().stream()
                .map(this::toRespDTO)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    /**
     * 搜索作品
     */
    public PageResult<PhotoRespDTO> searchPhotos(String keyword, Long categoryId, Long tagId, PageQuery query) {
        Page<Photo> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<Photo>()
                .eq(Photo::getStatus, "approved")
                .eq(categoryId != null, Photo::getCategoryId, categoryId)
                .like(keyword != null && !keyword.isEmpty(), Photo::getTitle, keyword)
                .orderByDesc(Photo::getCreateTime);

        if (tagId != null) {
            List<Long> photoIds = photoTagMapper.selectList(
                    new LambdaQueryWrapper<PhotoTag>().eq(PhotoTag::getTagId, tagId)
            ).stream().map(PhotoTag::getPhotoId).collect(Collectors.toList());

            if (photoIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPage(), query.getPageSize());
            }
            wrapper.in(Photo::getId, photoIds);
        }

        photoMapper.selectPage(page, wrapper);

        List<PhotoRespDTO> list = page.getRecords().stream()
                .map(this::toRespDTO)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    /**
     * 作品详情
     */
    public PhotoRespDTO getPhotoDetail(Long id) {
        Photo photo = photoMapper.selectById(id);
        if (photo == null || !"approved".equals(photo.getStatus())) {
            throw new BizException(404, "作品不存在");
        }

        PhotoRespDTO dto = toRespDTO(photo);

        // 判断当前用户是否已购买
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            Long orderCount = orderMapper.selectCount(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getUserId, userId)
                            .eq(Order::getPhotoId, id));
            dto.setIsPurchased(orderCount > 0);
        } else {
            dto.setIsPurchased(false);
        }

        return dto;
    }

    /**
     * 积分购买作品
     */
    @Transactional(rollbackFor = Exception.class)
    public BuyPhotoRespDTO buyPhoto(Long photoId) {
        Long userId = StpUtil.getLoginIdAsLong();

        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !"approved".equals(photo.getStatus())) {
            throw new BizException(404, "作品不存在或未上架");
        }

        // 不能购买自己的作品
        if (photo.getUserId().equals(userId)) {
            throw new BizException(409, "不能购买自己的作品");
        }

        // 检查是否已购买
        Long existCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getPhotoId, photoId));
        if (existCount > 0) {
            throw new BizException(409, "已购买过该作品");
        }

        // 检查积分
        User buyer = userMapper.selectById(userId);
        if (buyer.getPoints() < photo.getPrice()) {
            throw new BizException(409, "积分不足");
        }

        int price = photo.getPrice();
        int photographerEarn = (int) (price * 0.9); // 摄影师得 90%

        // 扣减买家积分
        buyer.setPoints(buyer.getPoints() - price);
        userMapper.updateById(buyer);

        // 增加摄影师积分
        User photographer = userMapper.selectById(photo.getUserId());
        photographer.setPoints(photographer.getPoints() + photographerEarn);
        userMapper.updateById(photographer);

        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setPhotoId(photoId);
        order.setPhotoTitle(photo.getTitle());
        order.setPhotoPreviewUrl(photo.getPreviewUrl());
        order.setPrice(price);
        order.setCreateBy(userId);
        order.setUpdateBy(userId);
        orderMapper.insert(order);

        // 更新购买次数
        photo.setPurchaseCount(photo.getPurchaseCount() + 1);
        photoMapper.updateById(photo);

        // 买家积分流水 - 支出
        PointsRecord spendRecord = new PointsRecord();
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
        pointsRecordMapper.insert(spendRecord);

        // 摄影师积分流水 - 收入
        PointsRecord earnRecord = new PointsRecord();
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
        pointsRecordMapper.insert(earnRecord);

        BuyPhotoRespDTO resp = new BuyPhotoRespDTO();
        resp.setOrderId(String.valueOf(order.getId()));
        resp.setPointsSpent(price);
        resp.setRemainingPoints(buyer.getPoints());
        return resp;
    }

    /**
     * 获取原图下载链接
     */
    public DownloadRespDTO getDownloadUrl(Long photoId) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        Photo photo = photoMapper.selectById(photoId);

        if (photo == null) {
            throw new BizException(404, "作品不存在");
        }

        // 权限校验：已购买 / 摄影师本人 / 管理员
        boolean isOwner = photo.getUserId().equals(userId);
        boolean isAdmin = "admin".equals(user.getRole());
        boolean hasPurchased = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getPhotoId, photoId)) > 0;

        if (!isOwner && !isAdmin && !hasPurchased) {
            throw new BizException(403, "无权下载该作品");
        }

        // 生成临时下载链接（实际项目中应调用 OSS SDK 生成签名 URL）
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(5);
        String url = ossHost + "/" + photo.getOriginalKey() + "?expire=" + expireAt.format(ISO_FMT);

        DownloadRespDTO resp = new DownloadRespDTO();
        resp.setUrl(url);
        resp.setExpireAt(expireAt.format(ISO_FMT));
        return resp;
    }

    /**
     * Photo -> PhotoRespDTO
     */
    private PhotoRespDTO toRespDTO(Photo photo) {
        PhotoRespDTO dto = new PhotoRespDTO();
        dto.setId(String.valueOf(photo.getId()));
        dto.setTitle(photo.getTitle());
        dto.setDescription(photo.getDescription());
        dto.setPreviewUrl(photo.getPreviewUrl());
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

        // 摄影师信息
        User photographer = userMapper.selectById(photo.getUserId());
        if (photographer != null) {
            PhotoRespDTO.PhotographerInfo info = new PhotoRespDTO.PhotographerInfo();
            info.setId(String.valueOf(photographer.getId()));
            info.setNickname(photographer.getNickname());
            info.setAvatar(photographer.getAvatar());
            dto.setPhotographer(info);
        }

        return dto;
    }
}
