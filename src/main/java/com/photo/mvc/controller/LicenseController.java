package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.R;
import com.photo.mvc.entity.req.LicenseCreateReq;
import com.photo.mvc.service.BizLicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "授权申请", description = "提交/查看/下载授权证书")
@RestController
@RequestMapping("/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final BizLicenseService bizLicenseService;

    @Operation(summary = "提交授权申请")
    @SaCheckLogin
    @PostMapping
    public R<Map<String, Object>> create(@Valid @RequestBody LicenseCreateReq req) {
        return R.ok(bizLicenseService.createLicense(req));
    }

    @Operation(summary = "授权申请详情")
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(bizLicenseService.getLicenseDetail(id));
    }

    @Operation(summary = "下载授权证书")
    @SaCheckLogin
    @GetMapping("/{id}/certificate")
    public R<Map<String, Object>> certificate(@PathVariable Long id) {
        return R.ok(bizLicenseService.getCertificateUrl(id));
    }
}
