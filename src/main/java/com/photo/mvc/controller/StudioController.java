package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.vo.PhotoVO;
import com.photo.mvc.entity.req.StudioLicenseReviewReq;
import com.photo.mvc.entity.req.StudioPhotoReq;
import com.photo.mvc.service.BizStudioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/studio")
@RequiredArgsConstructor
public class StudioController {

    private final BizStudioService bizStudioService;

    @SaCheckLogin
    @GetMapping("/photos")
    public R<PageResult<PhotoVO>> listMyPhotos(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(bizStudioService.listMyPhotos(status, query));
    }

    @SaCheckLogin
    @PostMapping("/photos")
    public R<Map<String, Object>> createPhoto(@Valid @RequestBody StudioPhotoReq req) {
        return R.ok(bizStudioService.createPhoto(req));
    }

    @SaCheckLogin
    @PutMapping("/photos/{id}")
    public R<Map<String, Object>> updatePhoto(
            @PathVariable Long id,
            @Valid @RequestBody StudioPhotoReq req) {
        return R.ok(bizStudioService.updatePhoto(id, req));
    }

    @SaCheckLogin
    @DeleteMapping("/photos/{id}")
    public R<Void> deletePhoto(@PathVariable Long id) {
        bizStudioService.deletePhoto(id);
        return R.ok(null);
    }

    @SaCheckLogin
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(bizStudioService.getSalesStats());
    }

    @SaCheckLogin
    @GetMapping("/earnings")
    public R<PageResult<Map<String, Object>>> earnings(PageQuery query) {
        return R.ok(bizStudioService.getEarnings(query));
    }

    @SaCheckLogin
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> licenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(bizStudioService.listReceivedLicenses(status, query));
    }

    @SaCheckLogin
    @PostMapping("/licenses/{id}/review")
    public R<Map<String, Object>> reviewLicense(
            @PathVariable Long id,
            @Valid @RequestBody StudioLicenseReviewReq req) {
        return R.ok(bizStudioService.reviewLicense(id, req));
    }
}
