package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.vo.BuyPhotoVO;
import com.photo.mvc.entity.vo.DownloadVO;
import com.photo.mvc.entity.vo.PhotoVO;
import com.photo.mvc.service.BizPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final BizPhotoService bizPhotoService;

    @GetMapping
    public R<PageResult<PhotoVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            PageQuery query) {
        return R.ok(bizPhotoService.listPhotos(categoryId, tagId, query));
    }

    @GetMapping("/search")
    public R<PageResult<PhotoVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            PageQuery query) {
        return R.ok(bizPhotoService.searchPhotos(keyword, categoryId, tagId, query));
    }

    @GetMapping("/{id}")
    public R<PhotoVO> detail(@PathVariable Long id) {
        return R.ok(bizPhotoService.getPhotoDetail(id));
    }

    @SaCheckLogin
    @PostMapping("/{id}/buy")
    public R<BuyPhotoVO> buy(@PathVariable Long id) {
        return R.ok(bizPhotoService.buyPhoto(id));
    }

    @SaCheckLogin
    @GetMapping("/{id}/download")
    public R<DownloadVO> download(@PathVariable Long id) {
        return R.ok(bizPhotoService.getDownloadUrl(id));
    }
}
