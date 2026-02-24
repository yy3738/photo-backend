package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.vo.UserVO;
import com.photo.mvc.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @SaCheckLogin
    @GetMapping("/me")
    public R<UserVO> me() {
        return R.ok(sysUserService.getCurrentUser());
    }

    @SaCheckLogin
    @PostMapping("/enable-photographer")
    public R<Map<String, String>> enablePhotographer() {
        return R.ok(sysUserService.enablePhotographer());
    }

    @SaCheckLogin
    @GetMapping("/points-history")
    public R<PageResult<Map<String, Object>>> pointsHistory(PageQuery query) {
        return R.ok(sysUserService.getPointsHistory(query));
    }

    @SaCheckLogin
    @GetMapping("/orders")
    public R<PageResult<Map<String, Object>>> orders(PageQuery query) {
        return R.ok(sysUserService.getOrders(query));
    }

    @SaCheckLogin
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> licenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(sysUserService.getLicenses(status, query));
    }
}
