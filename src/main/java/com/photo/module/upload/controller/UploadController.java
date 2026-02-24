package com.photo.module.upload.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.R;
import com.photo.module.upload.dto.UploadSignatureRespDTO;
import com.photo.module.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @SaCheckLogin
    @GetMapping("/signature")
    public R<UploadSignatureRespDTO> signature(
            @RequestParam String filename,
            @RequestParam String type) {
        return R.ok(uploadService.getSignature(filename, type));
    }
}
