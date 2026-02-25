package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.req.*;
import com.photo.mvc.entity.vo.*;
import com.photo.mvc.service.AdminService;
import com.photo.mvc.service.SysMenuService;
import com.photo.mvc.service.SysOrgService;
import com.photo.mvc.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理员", description = "仪表盘/用户管理/作品审核/授权审核/分类标签CRUD/积分充值/RBAC")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;
    private final SysOrgService sysOrgService;

    // ==================== 原有接口 ====================

    @Operation(summary = "仪表盘数据")
    @SaCheckRole("admin")
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        return R.ok(adminService.getDashboard());
    }

    @Operation(summary = "用户列表")
    @SaCheckRole("admin")
    @GetMapping("/users")
    public R<PageResult<Map<String, Object>>> listUsers(
            @RequestParam(required = false) String role,
            PageQuery query) {
        return R.ok(adminService.listUsers(role, query));
    }

    @Operation(summary = "封禁用户")
    @SaCheckRole("admin")
    @PostMapping("/users/{id}/ban")
    public R<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return R.ok(null);
    }

    @Operation(summary = "解封用户")
    @SaCheckRole("admin")
    @PostMapping("/users/{id}/unban")
    public R<Void> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return R.ok(null);
    }

    @Operation(summary = "积分充值")
    @SaCheckRole("admin")
    @PostMapping("/users/recharge")
    public R<Map<String, Object>> recharge(@Valid @RequestBody AdminRechargeReq req) {
        return R.ok(adminService.rechargePoints(req));
    }

    @Operation(summary = "待审核作品列表")
    @SaCheckRole("admin")
    @GetMapping("/photos")
    public R<PageResult<Map<String, Object>>> listPhotos(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(adminService.listPhotosForReview(status, query));
    }

    @Operation(summary = "审核作品")
    @SaCheckRole("admin")
    @PostMapping("/photos/{id}/review")
    public R<Map<String, Object>> reviewPhoto(
            @PathVariable Long id,
            @Valid @RequestBody AdminPhotoReviewReq req) {
        return R.ok(adminService.reviewPhoto(id, req));
    }

    @Operation(summary = "待审核授权列表")
    @SaCheckRole("admin")
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> listLicenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(adminService.listLicensesForReview(status, query));
    }

    @Operation(summary = "审核授权申请")
    @SaCheckRole("admin")
    @PostMapping("/licenses/{id}/review")
    public R<Map<String, Object>> reviewLicense(
            @PathVariable Long id,
            @Valid @RequestBody AdminLicenseReviewReq req) {
        return R.ok(adminService.reviewLicense(id, req));
    }

    @Operation(summary = "分类列表")
    @SaCheckRole("admin")
    @GetMapping("/categories")
    public R<List<CategoryVO>> listCategories() {
        return R.ok(adminService.listCategories());
    }

    @Operation(summary = "新建分类")
    @SaCheckRole("admin")
    @PostMapping("/categories")
    public R<Map<String, Object>> createCategory(@Valid @RequestBody AdminCategoryReq req) {
        return R.ok(adminService.createCategory(req));
    }

    @Operation(summary = "编辑分类")
    @SaCheckRole("admin")
    @PutMapping("/categories/{id}")
    public R<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategoryReq req) {
        return R.ok(adminService.updateCategory(id, req));
    }

    @Operation(summary = "删除分类")
    @SaCheckRole("admin")
    @DeleteMapping("/categories/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return R.ok(null);
    }

    @Operation(summary = "标签列表")
    @SaCheckRole("admin")
    @GetMapping("/tags")
    public R<List<TagVO>> listTags(@RequestParam(required = false) Long categoryId) {
        return R.ok(adminService.listTags(categoryId));
    }

    @Operation(summary = "新建标签")
    @SaCheckRole("admin")
    @PostMapping("/tags")
    public R<Map<String, Object>> createTag(@Valid @RequestBody AdminTagReq req) {
        return R.ok(adminService.createTag(req));
    }

    @Operation(summary = "编辑标签")
    @SaCheckRole("admin")
    @PutMapping("/tags/{id}")
    public R<Map<String, Object>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody AdminTagReq req) {
        return R.ok(adminService.updateTag(id, req));
    }

    @Operation(summary = "删除标签")
    @SaCheckRole("admin")
    @DeleteMapping("/tags/{id}")
    public R<Void> deleteTag(@PathVariable Long id) {
        adminService.deleteTag(id);
        return R.ok(null);
    }

    // ==================== 角色管理 ====================

    @Operation(summary = "角色分页列表")
    @SaCheckRole("admin")
    @GetMapping("/roles")
    public R<PageResult<RoleVO>> listRoles(PageQuery query) {
        return R.ok(sysRoleService.listRoles(query));
    }

    @Operation(summary = "角色详情")
    @SaCheckRole("admin")
    @GetMapping("/roles/{id}")
    public R<RoleVO> getRoleDetail(@PathVariable Long id) {
        return R.ok(sysRoleService.getRoleDetail(id));
    }

    @Operation(summary = "新建角色")
    @SaCheckRole("admin")
    @PostMapping("/roles")
    public R<RoleVO> createRole(@Valid @RequestBody AdminRoleReq req) {
        return R.ok(sysRoleService.createRole(req));
    }

    @Operation(summary = "编辑角色")
    @SaCheckRole("admin")
    @PutMapping("/roles/{id}")
    public R<RoleVO> updateRole(@PathVariable Long id, @Valid @RequestBody AdminRoleReq req) {
        return R.ok(sysRoleService.updateRole(id, req));
    }

    @Operation(summary = "启用/禁用角色")
    @SaCheckRole("admin")
    @PutMapping("/roles/{id}/status")
    public R<Void> updateRoleStatus(@PathVariable Long id, @Valid @RequestBody AdminStatusReq req) {
        sysRoleService.updateRoleStatus(id, req);
        return R.ok(null);
    }

    @Operation(summary = "删除角色")
    @SaCheckRole("admin")
    @DeleteMapping("/roles/{id}")
    public R<Void> deleteRole(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return R.ok(null);
    }

    // ==================== 用户角色分配 ====================

    @Operation(summary = "查询用户角色")
    @SaCheckRole("admin")
    @GetMapping("/users/{id}/roles")
    public R<List<String>> getUserRoles(@PathVariable Long id) {
        return R.ok(sysRoleService.getUserRoleIds(id));
    }

    @Operation(summary = "分配用户角色")
    @SaCheckRole("admin")
    @PutMapping("/users/{id}/roles")
    public R<Void> assignUserRoles(@PathVariable Long id, @Valid @RequestBody AdminUserRoleReq req) {
        sysRoleService.assignUserRoles(id, req.getRoleIds());
        return R.ok(null);
    }

    // ==================== 菜单管理 ====================

    @Operation(summary = "菜单树")
    @SaCheckRole("admin")
    @GetMapping("/menus")
    public R<List<MenuVO>> getMenuTree() {
        return R.ok(sysMenuService.getMenuTree());
    }

    @Operation(summary = "新建菜单")
    @SaCheckRole("admin")
    @PostMapping("/menus")
    public R<MenuVO> createMenu(@Valid @RequestBody AdminMenuReq req) {
        return R.ok(sysMenuService.createMenu(req));
    }

    @Operation(summary = "编辑菜单")
    @SaCheckRole("admin")
    @PutMapping("/menus/{id}")
    public R<MenuVO> updateMenu(@PathVariable Long id, @Valid @RequestBody AdminMenuReq req) {
        return R.ok(sysMenuService.updateMenu(id, req));
    }

    @Operation(summary = "删除菜单")
    @SaCheckRole("admin")
    @DeleteMapping("/menus/{id}")
    public R<Void> deleteMenu(@PathVariable Long id) {
        sysMenuService.deleteMenu(id);
        return R.ok(null);
    }

    // ==================== 组织管理 ====================

    @Operation(summary = "组织树")
    @SaCheckRole("admin")
    @GetMapping("/orgs")
    public R<List<OrgVO>> getOrgTree() {
        return R.ok(sysOrgService.getOrgTree());
    }

    @Operation(summary = "新建组织")
    @SaCheckRole("admin")
    @PostMapping("/orgs")
    public R<OrgVO> createOrg(@Valid @RequestBody AdminOrgReq req) {
        return R.ok(sysOrgService.createOrg(req));
    }

    @Operation(summary = "编辑组织")
    @SaCheckRole("admin")
    @PutMapping("/orgs/{id}")
    public R<OrgVO> updateOrg(@PathVariable Long id, @Valid @RequestBody AdminOrgReq req) {
        return R.ok(sysOrgService.updateOrg(id, req));
    }

    @Operation(summary = "启用/禁用组织")
    @SaCheckRole("admin")
    @PutMapping("/orgs/{id}/status")
    public R<Void> updateOrgStatus(@PathVariable Long id, @Valid @RequestBody AdminStatusReq req) {
        sysOrgService.updateOrgStatus(id, req);
        return R.ok(null);
    }

    @Operation(summary = "删除组织")
    @SaCheckRole("admin")
    @DeleteMapping("/orgs/{id}")
    public R<Void> deleteOrg(@PathVariable Long id) {
        sysOrgService.deleteOrg(id);
        return R.ok(null);
    }

    // ==================== 用户组织分配 ====================

    @Operation(summary = "分配用户组织")
    @SaCheckRole("admin")
    @PutMapping("/users/{id}/org")
    public R<Void> assignUserOrg(@PathVariable Long id, @Valid @RequestBody AdminUserOrgReq req) {
        sysOrgService.assignUserOrg(id, req.getOrgId());
        return R.ok(null);
    }
}
