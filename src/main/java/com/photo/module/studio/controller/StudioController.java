package com.photo.module.studio.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.module.photo.dto.PhotoRespDTO;
import com.photo.module.studio.dto.StudioLicenseReviewReqDTO;
import com.photo.module.studio.dto.StudioPhotoReqDTO;
import com.photo.module.studio.service.StudioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/studio")
@RequiredArgsConstructor
public class StudioController {

    private final StudioService studioService;

    @SaCheckLogin
    @GetMapping("/photos")
    public R<PageResult<PhotoRespDTO>> listMyPhotos(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(studioService.listMyPhotos(status, query));
    }

    @SaCheckLogin
    @PostMapping("/photos")
    public R<Map<String, Object>> createPhoto(@Valid @RequestBody StudioPhotoReqDTO req) {
        return R.ok(studioService.createPhoto(req));
    }

    @SaCheckLogin
    @PutMapping("/photos/{id}")
    public R<Map<String, Object>> updatePhoto(
            @PathVariable Long id,
            @Valid @RequestBody StudioPhotoReqDTO req) {
        return R.ok(studioService.updatePhoto(id, req));
    }

    @SaCheckLogin
    @DeleteMapping("/photos/{id}")
    public R<Void> deletePhoto(@PathVariable Long id) {
        studioService.deletePhoto(id);
        return R.ok(null);
    }

    @SaCheckLogin
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(studioService.getSalesStats());
    }

    @SaCheckLogin
    @GetMapping("/earnings")
    public R<PageResult<Map<String, Object>>> earnings(PageQuery query) {
        return R.ok(studioService.getEarnings(query));
    }

    @SaCheckLogin
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> licenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(studioService.listReceivedLicenses(status, query));
    }

    @SaCheckLogin
    @PostMapping("/licenses/{id}/review")
    public R<Map<String, Object>> reviewLicense(
            @PathVariable Long id,
            @Valid @RequestBody StudioLicenseReviewReqDTO req) {
        return R.ok(studioService.reviewLicense(id, req));
    }
}
