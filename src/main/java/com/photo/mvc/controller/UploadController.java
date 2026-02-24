package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.R;
import com.photo.mvc.entity.vo.UploadSignatureVO;
import com.photo.mvc.service.BizUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final BizUploadService bizUploadService;

    @SaCheckLogin
    @GetMapping("/signature")
    public R<UploadSignatureVO> signature(
            @RequestParam String filename,
            @RequestParam String type) {
        return R.ok(bizUploadService.getSignature(filename, type));
    }
}
