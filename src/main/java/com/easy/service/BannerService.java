package com.easy.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.entity.Banner;
import com.easy.pojo.vo.BannerVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
public interface BannerService extends IService<Banner> {

    // 获取轮播图列表
    Result<PageResult> getAllBanners(Integer bannerStatus);

    // 添加轮播图
    Result addBanner(MultipartFile  banner);

    // 更新轮播图
    Result updateBanner(Long bannerId, MultipartFile  banner);

    // 更新轮播图状态
    Result updateBannerStatus(Long bannerId, Integer bannerStatus);

    // 删除轮播图
    Result deleteBanner(Long bannerId);

    // 批量删除轮播图
    Result deleteBanners(List<Long> bannerIds);

    // 获取轮播图列表（用户端）
    List<BannerVO> getBannerList();

    List<Banner> listActiveBanners();
}
