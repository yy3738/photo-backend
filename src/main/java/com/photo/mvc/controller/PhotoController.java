package com.photo.mvc.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.common.result.R;
import com.photo.mvc.entity.vo.BuyPhotoVO;
import com.photo.mvc.entity.vo.DownloadVO;
import com.photo.mvc.entity.vo.PhotoVO;
import com.photo.mvc.service.BizPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "作品", description = "公开作品列表/详情/搜索/购买/下载")
@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final BizPhotoService bizPhotoService;

    @Operation(summary = "作品列表")
    @GetMapping
    public R<PageResult<PhotoVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            PageQuery query) {
        return R.ok(bizPhotoService.listPhotos(categoryId, tagId, query));
    }

    @Operation(summary = "搜索作品")
    @GetMapping("/search")
    public R<PageResult<PhotoVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            PageQuery query) {
        return R.ok(bizPhotoService.searchPhotos(keyword, categoryId, tagId, query));
    }

    @Operation(summary = "作品详情")
    @GetMapping("/{id}")
    public R<PhotoVO> detail(@PathVariable Long id) {
        return R.ok(bizPhotoService.getPhotoDetail(id));
    }

    @Operation(summary = "购买作品")
    @SaCheckLogin
    @PostMapping("/{id}/buy")
    public R<BuyPhotoVO> buy(@PathVariable Long id) {
        return R.ok(bizPhotoService.buyPhoto(id));
    }

    @Operation(summary = "获取作品下载链接")
    @SaCheckLogin
    @GetMapping("/{id}/download")
    public R<DownloadVO> download(@PathVariable Long id) {
        return R.ok(bizPhotoService.getDownloadUrl(id));
    }
}
