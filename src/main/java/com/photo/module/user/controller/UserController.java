package com.photo.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.module.user.dto.UserRespDTO;
import com.photo.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @SaCheckLogin
    @GetMapping("/me")
    public R<UserRespDTO> me() {
        return R.ok(userService.getCurrentUser());
    }

    @SaCheckLogin
    @PostMapping("/enable-photographer")
    public R<Map<String, String>> enablePhotographer() {
        return R.ok(userService.enablePhotographer());
    }

    @SaCheckLogin
    @GetMapping("/points-history")
    public R<PageResult<Map<String, Object>>> pointsHistory(PageQuery query) {
        return R.ok(userService.getPointsHistory(query));
    }

    @SaCheckLogin
    @GetMapping("/orders")
    public R<PageResult<Map<String, Object>>> orders(PageQuery query) {
        return R.ok(userService.getOrders(query));
    }

    @SaCheckLogin
    @GetMapping("/licenses")
    public R<PageResult<Map<String, Object>>> licenses(
            @RequestParam(required = false) String status,
            PageQuery query) {
        return R.ok(userService.getLicenses(status, query));
    }
}
