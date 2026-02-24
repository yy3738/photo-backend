package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.req.*;
import com.photo.mvc.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @SaCheckLogin
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        return R.ok(adminService.getDashboard());
    }

    @SaCheckLogin
    @GetMapping("/users")
    public R<PageResult<Map<String, Object>>> listUsers(
            @RequestParam(required = false) String role,
            PageQuery query) {
        return R.ok(adminService.listUsers(role, query));
    }

    @SaCheckLogin
    @PostMapping("/users/{id}/ban")
    public R<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return R.ok(null);
    }

    @SaCheckLogin
    @PostMapping("/users/{id}/unban")
    public R<Void> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return R.ok(null);
    }

    @SaCheckLogin
    @PostMapping("/users/recharge")
    public R<Map<String, Object>> recharge(@Valid @RequestBody AdminRechargeReq req) {
        return R.ok(adminService.rechargePoints(req));
    }

    @SaCheckLogin
    @GetMapping("/photos")
    public R<PageResult<Map<String, Object>>> listPhotos(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(adminService.listPhotosForReview(status, query));
    }

    @SaCheckLogin
    @PostMapping("/photos/{id}/review")
    public R<Map<String, Object>> reviewPhoto(
            @PathVariable Long id,
            @Valid @RequestBody AdminPhotoReviewReq req) {
        return R.ok(adminService.reviewPhoto(id, req));
    }

    @SaCheckLogin
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> listLicenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(adminService.listLicensesForReview(status, query));
    }

    @SaCheckLogin
    @PostMapping("/licenses/{id}/review")
    public R<Map<String, Object>> reviewLicense(
            @PathVariable Long id,
            @Valid @RequestBody AdminLicenseReviewReq req) {
        return R.ok(adminService.reviewLicense(id, req));
    }

    @SaCheckLogin
    @PostMapping("/categories")
    public R<Map<String, Object>> createCategory(@Valid @RequestBody AdminCategoryReq req) {
        return R.ok(adminService.createCategory(req));
    }

    @SaCheckLogin
    @PutMapping("/categories/{id}")
    public R<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategoryReq req) {
        return R.ok(adminService.updateCategory(id, req));
    }

    @SaCheckLogin
    @DeleteMapping("/categories/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return R.ok(null);
    }

    @SaCheckLogin
    @PostMapping("/tags")
    public R<Map<String, Object>> createTag(@Valid @RequestBody AdminTagReq req) {
        return R.ok(adminService.createTag(req));
    }

    @SaCheckLogin
    @PutMapping("/tags/{id}")
    public R<Map<String, Object>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody AdminTagReq req) {
        return R.ok(adminService.updateTag(id, req));
    }

    @SaCheckLogin
    @DeleteMapping("/tags/{id}")
    public R<Void> deleteTag(@PathVariable Long id) {
        adminService.deleteTag(id);
        return R.ok(null);
    }
}
