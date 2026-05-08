package com.easy.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.constant.MessageConstant;
import com.easy.enumeration.BannerStatusEnum;
import com.easy.mapper.BannerMapper;
import com.easy.pojo.dto.BannerDTO;
import com.easy.pojo.entity.Banner;
import com.easy.pojo.vo.BannerVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.BannerService;
import com.easy.service.MinIOService;
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

    private final MinIOService minioService;



    @Override
    @Cacheable(cacheNames = "bannerCache",
            key = "'admin:' + #bannerDTO.pageNum + ':' + #bannerDTO.pageSize + ':' + #bannerDTO.bannerStatus")
    public Result<PageResult<Banner>> getAllBanners(BannerDTO bannerDTO) {

        // 分页查询
        Page<Banner> page = new Page<>(bannerDTO.getPageNum(), bannerDTO.getPageSize());
        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        if (bannerDTO.getBannerStatus() != null) {
            queryWrapper.eq("status", bannerDTO.getBannerStatus().getId());
        }
        // 倒序排序
        queryWrapper.orderByDesc("id");

        IPage<Banner> bannerPage = baseMapper.selectPage(page, queryWrapper);
        if (bannerPage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        return Result.success(new PageResult<>(bannerPage.getTotal(), bannerPage.getRecords()));
    }


    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result addBanner(MultipartFile banner) {

        String bannerUrl = minioService.uploadFile(banner, "banner");

        Banner b = new Banner();
        b.setBannerUrl(bannerUrl).setBannerStatus(BannerStatusEnum.ENABLE);

        if (baseMapper.insert(b) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }


    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result updateBanner(Long bannerId, MultipartFile banner) {

        String bannerUrl = minioService.uploadFile(banner, "banner");

        Banner b = baseMapper.selectById(bannerId);
        String oldBannerUrl = b.getBannerUrl();
        if (oldBannerUrl != null && !oldBannerUrl.isEmpty()) {
            minioService.deleteFile(oldBannerUrl);
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


        // 确保轮播图状态有效
        BannerStatusEnum statusEnum;
        if (bannerStatus == 0) {
            statusEnum = BannerStatusEnum.ENABLE;
        } else if (bannerStatus == 1) {
            statusEnum = BannerStatusEnum.DISABLE;
        } else {
            return Result.error(MessageConstant.BANNER_STATUS_INVALID);
        }

        // 更新轮播图状态
        Banner banner = new Banner();
        banner.setBannerId(bannerId).setBannerStatus(statusEnum);

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
            minioService.deleteFile(bannerUrl);
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
        bannerUrlList.forEach(url -> minioService.deleteFile(url));

        if (baseMapper.deleteByIds(bannerIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 获取轮播图列表（用户端）
     *
     * @return 轮播图列表
     */
    @Override
    @Cacheable(cacheNames = "bannerCache",key = "'user:banner'")
    public Result<List<BannerVO>> getBannerList() {
        // 获取最后九个有效的轮播图
        List<Banner> banners = baseMapper.selectList(new QueryWrapper<Banner>()
                .eq("status", BannerStatusEnum.ENABLE.getId())
                .orderByDesc("id")
                .last("limit 9"));

        // 转换为VO
        List<BannerVO> bannerVOList = banners.stream()
                .map(banner -> {
                    BannerVO bannerVO = new BannerVO();
                    BeanUtils.copyProperties(banner, bannerVO);
                    return bannerVO;
                }).toList();

        return Result.success(bannerVOList);
    }

}
