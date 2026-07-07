package com.easy.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.constant.MessageConstant;
import com.easy.enumeration.BannerStatusEnum;
import com.easy.mapper.BannerMapper;
import com.easy.pojo.entity.Banner;
import com.easy.pojo.vo.BannerVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.BannerService;
import com.minio.MinioTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    private final MinioTemplate minioTemplate;



    @Override
    public Result<PageResult> getAllBanners(Integer bannerStatus) {

        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(bannerStatus!=null,Banner::getBannerStatus, bannerStatus);
        // 倒序排序
        queryWrapper.orderByDesc("id");

        List<Banner> banners = baseMapper.selectList(queryWrapper);
        if (banners.isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult(0L, null));
        }

        return Result.success(new PageResult((long) banners.size(), banners));
    }


    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result addBanner(MultipartFile banner) {

        String bannerUrl = minioTemplate.uploadFile(banner, "banner");

        Banner b = new Banner();
        b.setBannerUrl(bannerUrl).setBannerStatus(1);

        if (baseMapper.insert(b) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }


    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result updateBanner(Long bannerId, MultipartFile banner) {

        String bannerUrl = minioTemplate.uploadFile(banner, "banner");

        Banner b = baseMapper.selectById(bannerId);
        String oldBannerUrl = b.getBannerUrl();
        if (oldBannerUrl != null && !oldBannerUrl.isEmpty()) {
            minioTemplate.deleteFile(oldBannerUrl);
        }

        b.setBannerUrl(bannerUrl);
        if (baseMapper.updateById(b) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新轮播图状态
     *
     * @param bannerId     轮播图ID
     * @param bannerStatus 轮播图状态
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public Result updateBannerStatus(Long bannerId, Integer bannerStatus) {



        // 更新轮播图状态
        Banner banner = new Banner();
        banner.setBannerId(bannerId).setBannerStatus(bannerStatus);

        if (baseMapper.updateById(banner) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);

    }

    /**
     * 删除轮播图
     * @param bannerId 轮播图ID
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public Result deleteBanner(Long bannerId) {

        Banner banner = baseMapper.selectById(bannerId);
        if (banner == null) {
            return Result.error(MessageConstant.DATA_NOT_FOUND);
        }
        String bannerUrl = banner.getBannerUrl();
        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            minioTemplate.deleteFile(bannerUrl);
        }

        if (baseMapper.deleteById(bannerId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 轮播图ID列表
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public Result deleteBanners(List<Long> bannerIds) {


        List<Banner> banners = baseMapper.selectByIds(bannerIds);
        List<String> bannerUrlList = banners.stream()
                .map(Banner::getBannerUrl)
                .filter(url -> url != null && !url.isEmpty())
                .toList();
        bannerUrlList.forEach(url -> minioTemplate.deleteFile(url));

        if (baseMapper.deleteByIds(bannerIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    public List<BannerVO> getBannerList() {
        return List.of();
    }

    @Override
    public List<Banner> listActiveBanners() {
        return list(new QueryWrapper<Banner>()
                .eq("status", 1)
                .orderByDesc("id"));
    }


}
