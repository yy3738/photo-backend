package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.req.*;
import com.photo.mvc.entity.vo.CategoryVO;
import com.photo.mvc.entity.vo.TagVO;
import com.photo.mvc.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理员", description = "仪表盘/用户管理/作品审核/授权审核/分类标签CRUD/积分充值")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "仪表盘数据")
    @SaCheckLogin
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        return R.ok(adminService.getDashboard());
    }

    @Operation(summary = "用户列表")
    @SaCheckLogin
    @GetMapping("/users")
    public R<PageResult<Map<String, Object>>> listUsers(
            @RequestParam(required = false) String role,
            PageQuery query) {
        return R.ok(adminService.listUsers(role, query));
    }

    @Operation(summary = "封禁用户")
    @SaCheckLogin
    @PostMapping("/users/{id}/ban")
    public R<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return R.ok(null);
    }

    @Operation(summary = "解封用户")
    @SaCheckLogin
    @PostMapping("/users/{id}/unban")
    public R<Void> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return R.ok(null);
    }

    @Operation(summary = "积分充值")
    @SaCheckLogin
    @PostMapping("/users/recharge")
    public R<Map<String, Object>> recharge(@Valid @RequestBody AdminRechargeReq req) {
        return R.ok(adminService.rechargePoints(req));
    }

    @Operation(summary = "待审核作品列表")
    @SaCheckLogin
    @GetMapping("/photos")
    public R<PageResult<Map<String, Object>>> listPhotos(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(adminService.listPhotosForReview(status, query));
    }

    @Operation(summary = "审核作品")
    @SaCheckLogin
    @PostMapping("/photos/{id}/review")
    public R<Map<String, Object>> reviewPhoto(
            @PathVariable Long id,
            @Valid @RequestBody AdminPhotoReviewReq req) {
        return R.ok(adminService.reviewPhoto(id, req));
    }

    @Operation(summary = "待审核授权列表")
    @SaCheckLogin
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> listLicenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(adminService.listLicensesForReview(status, query));
    }

    @Operation(summary = "审核授权申请")
    @SaCheckLogin
    @PostMapping("/licenses/{id}/review")
    public R<Map<String, Object>> reviewLicense(
            @PathVariable Long id,
            @Valid @RequestBody AdminLicenseReviewReq req) {
        return R.ok(adminService.reviewLicense(id, req));
    }

    @Operation(summary = "分类列表")
    @SaCheckLogin
    @GetMapping("/categories")
    public R<List<CategoryVO>> listCategories() {
        return R.ok(adminService.listCategories());
    }

    @Operation(summary = "新建分类")
    @SaCheckLogin
    @PostMapping("/categories")
    public R<Map<String, Object>> createCategory(@Valid @RequestBody AdminCategoryReq req) {
        return R.ok(adminService.createCategory(req));
    }

    @Operation(summary = "编辑分类")
    @SaCheckLogin
    @PutMapping("/categories/{id}")
    public R<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategoryReq req) {
        return R.ok(adminService.updateCategory(id, req));
    }

    @Operation(summary = "删除分类")
    @SaCheckLogin
    @DeleteMapping("/categories/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return R.ok(null);
    }

    @Operation(summary = "标签列表")
    @SaCheckLogin
    @GetMapping("/tags")
    public R<List<TagVO>> listTags(@RequestParam(required = false) Long categoryId) {
        return R.ok(adminService.listTags(categoryId));
    }

    @Operation(summary = "新建标签")
    @SaCheckLogin
    @PostMapping("/tags")
    public R<Map<String, Object>> createTag(@Valid @RequestBody AdminTagReq req) {
        return R.ok(adminService.createTag(req));
    }

    @Operation(summary = "编辑标签")
    @SaCheckLogin
    @PutMapping("/tags/{id}")
    public R<Map<String, Object>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody AdminTagReq req) {
        return R.ok(adminService.updateTag(id, req));
    }

    @Operation(summary = "删除标签")
    @SaCheckLogin
    @DeleteMapping("/tags/{id}")
    public R<Void> deleteTag(@PathVariable Long id) {
        adminService.deleteTag(id);
        return R.ok(null);
    }
}
