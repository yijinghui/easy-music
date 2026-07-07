package com.easy.controller.admin;


import com.easy.annotation.LogOperation;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RequestMapping("/admin/banners")
@Tag(name = "Admin端-轮播图相关接口")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 获取轮播图列表
     *
     * @return 轮播图列表
     */
    @Operation(summary = "获取所有轮播图接口")
    @GetMapping("/list")
    public Result<PageResult> getAllBanners() {
        return bannerService.getAllBanners(null);
    }

    /**
     * 添加轮播图
     *
     * @param banner 轮播图
     * @return 结果
     */
    @Operation(summary = "添加轮播图接口")
    @PostMapping
    @LogOperation
    public Result addBanner(@RequestParam("banner") MultipartFile banner) {
        return bannerService.addBanner(banner);
    }

    /**
     * 更新轮播图
     *
     * @param banner 轮播图
     * @return 结果
     */
    @Operation(summary = "更新轮播图接口")
    @PatchMapping("/{id}")
    @LogOperation
    public Result updateBanner(@PathVariable("id") Long bannerId, @RequestParam("banner") MultipartFile banner) {
        return bannerService.updateBanner(bannerId, banner);
    }

    /**
     * 更新轮播图状态
     *
     * @param bannerStatus 轮播图状态
     * @return 结果
     */
    @Operation(summary = "更新轮播图状态接口")
    @PatchMapping("/status/{id}")
    @LogOperation
    public Result updateBannerStatus(@PathVariable("id") Long bannerId, @RequestParam("status") Integer bannerStatus) {
        return bannerService.updateBannerStatus(bannerId, bannerStatus);
    }

    /**
     * 删除轮播图
     *
     * @param bannerId 轮播图id
     * @return 结果
     */
    @Operation(summary = "删除轮播图接口")
    @DeleteMapping("/{id}")
    @LogOperation
    public Result deleteBanner(@PathVariable("id") Long bannerId) {
        return bannerService.deleteBanner(bannerId);
    }

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 轮播图id列表
     * @return 结果
     */
    @Operation(summary = "批量删除轮播图接口")
    @DeleteMapping("/batch")
    @LogOperation
    public Result deleteBanners(@RequestBody List<Long> bannerIds) {
        return bannerService.deleteBanners(bannerIds);
    }


}
