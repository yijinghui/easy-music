package com.easy.controller.admin;

import com.easy.annotation.LogOperation;
import com.easy.constant.MessageConstant;
import com.easy.pojo.dto.PageQueryDTO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
@Tag(name = "Admin端-轮播图相关接口")
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/list")
    @Operation(summary = "获取所有轮播图接口")
    public Result<PageResult> page(PageQueryDTO pageQueryDTO) {
        return Result.success(bannerService.page(pageQueryDTO));
    }

    @PostMapping
    @Operation(summary = "添加轮播图接口")
    @LogOperation
    public Result add(@RequestParam("banner") MultipartFile banner) {
        bannerService.add(banner);
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    @PatchMapping("/status/{id}")
    @Operation(summary = "更新轮播图状态接口")
    @LogOperation
    public Result updateStatus(@PathVariable("id") Long bannerId, @RequestParam("status") Integer bannerStatus) {
        bannerService.updateStatus(bannerId, bannerStatus);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除轮播图接口")
    @LogOperation
    public Result delete(@PathVariable("id") Long bannerId) {
        bannerService.delete(bannerId);
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }


    @DeleteMapping("/batch")
    @Operation(summary = "批量删除轮播图接口")
    @LogOperation
    public Result deleteByIds(@RequestBody List<Long> bannerIds) {
        bannerService.deleteByIds(bannerIds);
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }
}
