package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.exception.BaseException;
import com.easy.mapper.ArtistAuthMapper;
import com.easy.mapper.ArtistMapper;
import com.easy.mapper.UserMapper;
import com.easy.pojo.dto.ArtistAuthPageQueryDTO;
import com.easy.pojo.entity.Artist;
import com.easy.pojo.entity.ArtistAuth;
import com.easy.pojo.entity.User;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.ArtistAuthService;
import com.easy.utils.ThreadLocalUtil;
import com.minio.MinioTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ArtistAuthServiceImpl extends ServiceImpl<ArtistAuthMapper, ArtistAuth> implements ArtistAuthService  {



    @Override
    public PageResult page(ArtistAuthPageQueryDTO pageQueryDTO) {
        LocalDate createBeginTime=pageQueryDTO.getCreateStartTime();
        LocalDate createEndTime=pageQueryDTO.getCreateEndTime();
        LocalDate auditBeginTime=pageQueryDTO.getAuditStartTime();
        LocalDate auditEndTime=pageQueryDTO.getAuditEndTime();

        if (createBeginTime != null && createEndTime != null && createBeginTime.isAfter(createEndTime)){
            throw new BaseException("创建时间范围错误");
        }
        if (auditBeginTime != null && auditEndTime != null && auditBeginTime.isAfter(auditEndTime)){
            throw new BaseException("审核时间范围错误");
        }

        Page<ArtistAuth> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        LambdaQueryWrapper<ArtistAuth> queryWrapper = new LambdaQueryWrapper<ArtistAuth>()
                .eq(pageQueryDTO.getId() != null, ArtistAuth::getId, pageQueryDTO.getId())
                .eq(pageQueryDTO.getArtistId() != null, ArtistAuth::getArtistId, pageQueryDTO.getArtistId())
                .eq(pageQueryDTO.getUserId() != null, ArtistAuth::getUserId, pageQueryDTO.getUserId())
                .eq(pageQueryDTO.getStatus() != null, ArtistAuth::getStatus, pageQueryDTO.getStatus());

        if (createBeginTime != null) {
            queryWrapper.ge(ArtistAuth::getCreateTime, createBeginTime.atStartOfDay());
        }
        if (createEndTime != null) {
            queryWrapper.le(ArtistAuth::getCreateTime, createEndTime.atTime(23, 59, 59));
        }

        if (auditBeginTime != null) {
            queryWrapper.ge(ArtistAuth::getAuditTime, auditBeginTime.atStartOfDay());
        }
        if (auditEndTime != null) {
            queryWrapper.le(ArtistAuth::getAuditTime, auditEndTime.atTime(23, 59, 59));
        }

        queryWrapper.orderByDesc(ArtistAuth::getCreateTime);

        Page<ArtistAuth> artistAuthPage = baseMapper.selectPage(page, queryWrapper);

        return new PageResult(artistAuthPage.getTotal(), artistAuthPage.getRecords());
    }

    @Override
    public void audit(ArtistAuth auth) {
        // TODO 待开发
    }
}
