package com.photo.mvc.controller;

import com.photo.common.result.R;
import com.photo.mvc.entity.req.WxLoginReq;
import com.photo.mvc.entity.req.RefreshTokenReq;
import com.photo.mvc.entity.vo.LoginVO;
import com.photo.mvc.entity.vo.RefreshTokenVO;
import com.photo.mvc.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/wx-login")
    public R<LoginVO> wxLogin(@Valid @RequestBody WxLoginReq req) {
        return R.ok(authService.wxLogin(req.getCode()));
    }

    @PostMapping("/refresh")
    public R<RefreshTokenVO> refresh(@Valid @RequestBody RefreshTokenReq req) {
        return R.ok(authService.refreshToken(req.getRefreshToken()));
    }
}
