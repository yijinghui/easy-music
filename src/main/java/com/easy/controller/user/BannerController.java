package com.easy.controller.user;

import com.easy.pojo.vo.BannerVO;
import com.easy.result.Result;
import com.easy.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "C端-轮播图接口")
@RestController("userBannerController")
@RequestMapping("/user/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "获取轮播图列表接口")
    @GetMapping("/list")
    public Result<List<BannerVO>> getBannerList() {
        return bannerService.getBannerList();
    }
}
