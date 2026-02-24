package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.R;
import com.photo.mvc.entity.req.LicenseCreateReq;
import com.photo.mvc.service.BizLicenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final BizLicenseService bizLicenseService;

    @SaCheckLogin
    @PostMapping
    public R<Map<String, Object>> create(@Valid @RequestBody LicenseCreateReq req) {
        return R.ok(bizLicenseService.createLicense(req));
    }

    @SaCheckLogin
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(bizLicenseService.getLicenseDetail(id));
    }

    @SaCheckLogin
    @GetMapping("/{id}/certificate")
    public R<Map<String, Object>> certificate(@PathVariable Long id) {
        return R.ok(bizLicenseService.getCertificateUrl(id));
    }
}
