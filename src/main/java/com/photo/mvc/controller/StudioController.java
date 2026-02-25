package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.vo.PhotoVO;
import com.photo.mvc.entity.req.StudioLicenseReviewReq;
import com.photo.mvc.entity.req.StudioPhotoReq;
import com.photo.mvc.entity.req.StudioPhotoStatusReq;
import com.photo.mvc.service.BizStudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "摄影师工作台", description = "作品CRUD/销售统计/收入明细/授权审批")
@RestController
@RequestMapping("/studio")
@RequiredArgsConstructor
public class StudioController {

    private final BizStudioService bizStudioService;

    @Operation(summary = "我的作品列表")
    @SaCheckRole("photographer")
    @GetMapping("/photos")
    public R<PageResult<PhotoVO>> listMyPhotos(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(bizStudioService.listMyPhotos(status, query));
    }

    @Operation(summary = "上传作品")
    @SaCheckRole("photographer")
    @PostMapping("/photos")
    public R<Map<String, Object>> createPhoto(@Valid @RequestBody StudioPhotoReq req) {
        return R.ok(bizStudioService.createPhoto(req));
    }

    @Operation(summary = "编辑作品")
    @SaCheckRole("photographer")
    @PutMapping("/photos/{id}")
    public R<Map<String, Object>> updatePhoto(
            @PathVariable Long id,
            @Valid @RequestBody StudioPhotoReq req) {
        return R.ok(bizStudioService.updatePhoto(id, req));
    }

    @Operation(summary = "作品详情")
    @SaCheckRole("photographer")
    @GetMapping("/photos/{id}")
    public R<PhotoVO> getPhotoDetail(@PathVariable Long id) {
        return R.ok(bizStudioService.getMyPhotoDetail(id));
    }

    @Operation(summary = "上架/下架作品")
    @SaCheckRole("photographer")
    @PatchMapping("/photos/{id}/status")
    public R<Map<String, Object>> togglePhotoStatus(
            @PathVariable Long id,
            @Valid @RequestBody StudioPhotoStatusReq req) {
        return R.ok(bizStudioService.togglePhotoStatus(id, req));
    }

    @Operation(summary = "删除作品")
    @SaCheckRole("photographer")
    @DeleteMapping("/photos/{id}")
    public R<Void> deletePhoto(@PathVariable Long id) {
        bizStudioService.deletePhoto(id);
        return R.ok(null);
    }

    @Operation(summary = "销售统计")
    @SaCheckRole("photographer")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(bizStudioService.getSalesStats());
    }

    @Operation(summary = "收入明细")
    @SaCheckRole("photographer")
    @GetMapping("/earnings")
    public R<PageResult<Map<String, Object>>> earnings(PageQuery query) {
        return R.ok(bizStudioService.getEarnings(query));
    }

    @Operation(summary = "收到的授权申请列表")
    @SaCheckRole("photographer")
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> licenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(bizStudioService.listReceivedLicenses(status, query));
    }

    @Operation(summary = "审批授权申请")
    @SaCheckRole("photographer")
    @PostMapping("/licenses/{id}/review")
    public R<Map<String, Object>> reviewLicense(
            @PathVariable Long id,
            @Valid @RequestBody StudioLicenseReviewReq req) {
        return R.ok(bizStudioService.reviewLicense(id, req));
    }
}
