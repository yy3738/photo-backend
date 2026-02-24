package com.photo.module.photo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.module.photo.dto.BuyPhotoRespDTO;
import com.photo.module.photo.dto.DownloadRespDTO;
import com.photo.module.photo.dto.PhotoRespDTO;
import com.photo.module.photo.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @GetMapping
    public R<PageResult<PhotoRespDTO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            PageQuery query) {
        return R.ok(photoService.listPhotos(categoryId, tagId, query));
    }

    @GetMapping("/search")
    public R<PageResult<PhotoRespDTO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            PageQuery query) {
        return R.ok(photoService.searchPhotos(keyword, categoryId, tagId, query));
    }

    @GetMapping("/{id}")
    public R<PhotoRespDTO> detail(@PathVariable Long id) {
        return R.ok(photoService.getPhotoDetail(id));
    }

    @SaCheckLogin
    @PostMapping("/{id}/buy")
    public R<BuyPhotoRespDTO> buy(@PathVariable Long id) {
        return R.ok(photoService.buyPhoto(id));
    }

    @SaCheckLogin
    @GetMapping("/{id}/download")
    public R<DownloadRespDTO> download(@PathVariable Long id) {
        return R.ok(photoService.getDownloadUrl(id));
    }
}
