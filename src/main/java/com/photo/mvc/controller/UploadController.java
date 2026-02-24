package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.R;
import com.photo.mvc.entity.vo.UploadSignatureVO;
import com.photo.mvc.service.BizUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "上传", description = "OSS直传签名")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final BizUploadService bizUploadService;

    @Operation(summary = "获取OSS上传签名")
    @SaCheckLogin
    @GetMapping("/signature")
    public R<UploadSignatureVO> signature(
            @RequestParam String filename,
            @RequestParam String type) {
        return R.ok(bizUploadService.getSignature(filename, type));
    }
}
