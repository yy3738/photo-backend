package com.photo.module.license.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.R;
import com.photo.module.license.dto.LicenseCreateReqDTO;
import com.photo.module.license.service.LicenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;

    @SaCheckLogin
    @PostMapping
    public R<Map<String, Object>> create(@Valid @RequestBody LicenseCreateReqDTO req) {
        return R.ok(licenseService.createLicense(req));
    }

    @SaCheckLogin
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(licenseService.getLicenseDetail(id));
    }

    @SaCheckLogin
    @GetMapping("/{id}/certificate")
    public R<Map<String, Object>> certificate(@PathVariable Long id) {
        return R.ok(licenseService.getCertificateUrl(id));
    }
}
