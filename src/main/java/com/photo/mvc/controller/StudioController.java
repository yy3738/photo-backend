package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.vo.PhotoVO;
import com.photo.mvc.entity.req.StudioLicenseReviewReq;
import com.photo.mvc.entity.req.StudioPhotoReq;
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
    @SaCheckLogin
    @GetMapping("/photos")
    public R<PageResult<PhotoVO>> listMyPhotos(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(bizStudioService.listMyPhotos(status, query));
    }

    @Operation(summary = "上传作品")
    @SaCheckLogin
    @PostMapping("/photos")
    public R<Map<String, Object>> createPhoto(@Valid @RequestBody StudioPhotoReq req) {
        return R.ok(bizStudioService.createPhoto(req));
    }

    @Operation(summary = "编辑作品")
    @SaCheckLogin
    @PutMapping("/photos/{id}")
    public R<Map<String, Object>> updatePhoto(
            @PathVariable Long id,
            @Valid @RequestBody StudioPhotoReq req) {
        return R.ok(bizStudioService.updatePhoto(id, req));
    }

    @Operation(summary = "删除作品")
    @SaCheckLogin
    @DeleteMapping("/photos/{id}")
    public R<Void> deletePhoto(@PathVariable Long id) {
        bizStudioService.deletePhoto(id);
        return R.ok(null);
    }

    @Operation(summary = "销售统计")
    @SaCheckLogin
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(bizStudioService.getSalesStats());
    }

    @Operation(summary = "收入明细")
    @SaCheckLogin
    @GetMapping("/earnings")
    public R<PageResult<Map<String, Object>>> earnings(PageQuery query) {
        return R.ok(bizStudioService.getEarnings(query));
    }

    @Operation(summary = "收到的授权申请列表")
    @SaCheckLogin
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> licenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(bizStudioService.listReceivedLicenses(status, query));
    }

    @Operation(summary = "审批授权申请")
    @SaCheckLogin
    @PostMapping("/licenses/{id}/review")
    public R<Map<String, Object>> reviewLicense(
            @PathVariable Long id,
            @Valid @RequestBody StudioLicenseReviewReq req) {
        return R.ok(bizStudioService.reviewLicense(id, req));
    }
}
