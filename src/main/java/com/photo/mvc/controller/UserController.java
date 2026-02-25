package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.vo.UserVO;
import com.photo.mvc.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户中心", description = "个人信息/积分明细/订单/授权记录")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @Operation(summary = "获取当前用户信息")
    @SaCheckLogin
    @GetMapping("/me")
    public R<UserVO> me() {
        return R.ok(sysUserService.getCurrentUser());
    }

    @Operation(summary = "开通摄影师身份")
    @SaCheckLogin
    @PostMapping("/enable-photographer")
    public R<Map<String, Object>> enablePhotographer() {
        return R.ok(sysUserService.enablePhotographer());
    }

    @Operation(summary = "积分明细")
    @SaCheckLogin
    @GetMapping("/points-history")
    public R<PageResult<Map<String, Object>>> pointsHistory(PageQuery query) {
        return R.ok(sysUserService.getPointsHistory(query));
    }

    @Operation(summary = "我的订单列表")
    @SaCheckLogin
    @GetMapping("/orders")
    public R<PageResult<Map<String, Object>>> orders(PageQuery query) {
        return R.ok(sysUserService.getOrders(query));
    }

    @Operation(summary = "我的授权记录")
    @SaCheckLogin
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> licenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(sysUserService.getLicenses(status, query));
    }
}
