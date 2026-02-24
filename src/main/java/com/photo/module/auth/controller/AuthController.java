package com.photo.module.auth.controller;

import com.photo.common.result.R;
import com.photo.module.auth.dto.LoginRespDTO;
import com.photo.module.auth.dto.RefreshTokenReqDTO;
import com.photo.module.auth.dto.RefreshTokenRespDTO;
import com.photo.module.auth.dto.WxLoginReqDTO;
import com.photo.module.auth.service.AuthService;
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
    public R<LoginRespDTO> wxLogin(@Valid @RequestBody WxLoginReqDTO req) {
        return R.ok(authService.wxLogin(req.getCode()));
    }

    @PostMapping("/refresh")
    public R<RefreshTokenRespDTO> refresh(@Valid @RequestBody RefreshTokenReqDTO req) {
        return R.ok(authService.refreshToken(req.getRefreshToken()));
    }
}
