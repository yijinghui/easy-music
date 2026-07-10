package com.easy.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.constant.MessageConstant;
import com.easy.enumeration.BannerStatusEnum;
import com.easy.mapper.BannerMapper;
import com.easy.pojo.dto.PageQueryDTO;
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
    public PageResult page(PageQueryDTO pageQueryDTO) {
        IPage<Banner> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        lambdaQuery().page(page);
        return new PageResult(page.getTotal(), page.getRecords());
    }


    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void add(MultipartFile banner) {
        String bannerUrl = minioTemplate.uploadFile(banner, "banner");
        Banner b = new Banner();
        b.setBannerUrl(bannerUrl).setBannerStatus(1);
        save(b);
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
    public void updateStatus(Long bannerId, Integer bannerStatus) {
        // 更新轮播图状态
        Banner banner = new Banner();
        banner.setBannerId(bannerId).setBannerStatus(bannerStatus);
        baseMapper.updateById(banner);

    }

    /**
     * 删除轮播图
     * @param bannerId 轮播图ID
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public void delete(Long bannerId) {

        Banner banner = baseMapper.selectById(bannerId);
        if (banner == null) {
            return;
        }
        String bannerUrl = banner.getBannerUrl();
        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            minioTemplate.deleteFile(bannerUrl);
        }
        removeById(bannerId);
    }

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 轮播图ID列表
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "bannerCache", allEntries = true)
    public void deleteByIds(List<Long> bannerIds) {
        List<Banner> banners = baseMapper.selectByIds(bannerIds);
        if (banners.isEmpty()) {
            return;
        }
        List<String> bannerUrlList = banners.stream()
                .map(Banner::getBannerUrl)
                .filter(url -> url != null && !url.isEmpty())
                .toList();
        bannerUrlList.forEach(minioTemplate::deleteFile);

        removeByIds(bannerIds);

    }

    @Override
    public List<Banner> listActiveBanners() {
        return list(new QueryWrapper<Banner>()
                .eq("status", 1)
                .orderByDesc("id"));
    }


}
