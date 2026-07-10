package com.easy.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.PageQueryDTO;
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
    PageResult page(PageQueryDTO pageQueryDTO);

    // 添加轮播图
    void add(MultipartFile  banner);

    // 更新轮播图状态
    void updateStatus(Long bannerId, Integer bannerStatus);

    // 删除轮播图
    void delete(Long bannerId);

    // 批量删除轮播图
    void deleteByIds(List<Long> bannerIds);

    List<Banner> listActiveBanners();
}
