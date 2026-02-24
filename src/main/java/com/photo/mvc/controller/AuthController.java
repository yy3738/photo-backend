package com.photo.mvc.controller;

import com.photo.common.result.R;
import com.photo.mvc.entity.req.WxLoginReq;
import com.photo.mvc.entity.req.RefreshTokenReq;
import com.photo.mvc.entity.vo.LoginVO;
import com.photo.mvc.entity.vo.RefreshTokenVO;
import com.photo.mvc.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证", description = "微信登录与Token刷新")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "微信小程序登录")
    @PostMapping("/wx-login")
    public R<LoginVO> wxLogin(@Valid @RequestBody WxLoginReq req) {
        return R.ok(authService.wxLogin(req.getCode()));
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public R<RefreshTokenVO> refresh(@Valid @RequestBody RefreshTokenReq req) {
        return R.ok(authService.refreshToken(req.getRefreshToken()));
    }
}
