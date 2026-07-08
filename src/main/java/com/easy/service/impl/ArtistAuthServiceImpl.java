package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    private final ArtistMapper artistMapper;
    private final MinioTemplate minioTemplate;
    private final UserMapper userMapper;


    @Override
    public Result<PageResult> getArtistAuth(ArtistAuthPageQueryDTO pageQueryDTO) {
        LocalDate createBeginTime=pageQueryDTO.getCreateStartTime();
        LocalDate createEndTime=pageQueryDTO.getCreateEndTime();
        LocalDate auditBeginTime=pageQueryDTO.getAuditStartTime();
        LocalDate auditEndTime=pageQueryDTO.getAuditEndTime();

        if (createBeginTime != null && createEndTime != null && createBeginTime.isAfter(createEndTime)){
            return Result.error("创建时间范围错误",  null);
        }
        if (auditBeginTime != null && auditEndTime != null && auditBeginTime.isAfter(auditEndTime)){
            return Result.error("审核时间范围错误", null);
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
        return Result.success(new PageResult(artistAuthPage.getTotal(), artistAuthPage.getRecords()));
    }

    @Override
    public Result deleteArtistAuth(Long id) {
        if (id == null) {
            return Result.error("id不能为空", null);
        }
        ArtistAuth artistAuth = getById(id);
        minioTemplate.deleteFile(artistAuth.getBusinessLicenseUrl());
        return removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result updateArtistAuth(ArtistAuth auth) {

        if (auth.getUserId()==null){
            return Result.error("用户id不能为空");
        }
        if (auth.getArtistId()==null){
            return Result.error("歌手id不能为空");
        }
        if (userMapper.selectCount(new QueryWrapper<User>().eq("id",auth.getUserId()))==0){
            return Result.error("用户不存在");
        }

        if (artistMapper.selectCount(new QueryWrapper<Artist>().eq("id",auth.getArtistId()))==0){
            return Result.error("歌手不存在");
        }

        Long id=auth.getId();
        ArtistAuth at = baseMapper.selectById(id);
        Integer status=auth.getStatus();
        Integer oldStatus=at.getStatus();
        BeanUtil.copyProperties(auth, at);


        if (status.equals(oldStatus)){
            return baseMapper.updateById(at) > 0 ? Result.success("更新成功") : Result.error("更新失败");
        }

        // 更新歌手状态
        Artist artist = new Artist();
        artist.setArtistId(auth.getArtistId());
        artist.setStatus(status);
        at.setAuditTime(LocalDateTime.now());
        if (status.equals(2) && oldStatus.equals(1)) { // 认证通过
            artist.setUserId(auth.getUserId());
            artistMapper.updateById(artist);
        } else if ((status.equals(1) && oldStatus.equals(2)) || (status.equals(0) && oldStatus.equals(2))) {
            LambdaUpdateWrapper<Artist> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Artist::getArtistId, auth.getArtistId())
                    .set(Artist::getStatus, status)
                    .set(Artist::getUserId, null);
            artistMapper.update(null, updateWrapper);
        }

        return baseMapper.updateById(at) > 0 ? Result.success("审核状态更新成功") : Result.error("审核状态更新失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result addArtistAuth(ArtistAuth auth) {
        Long artistId=auth.getArtistId();
        Long userId=auth.getUserId();
        if (userId==null){
            return Result.error("用户id不能为空");
        }
        if (artistId==null){
            return Result.error("歌手id不能为空");
        }
        if (userMapper.selectCount(new QueryWrapper<User>().eq("id",userId))==0){
            return Result.error("用户不存在");
        }

        if (artistMapper.selectCount(new QueryWrapper<Artist>().eq("id",artistId))==0){
            return Result.error("歌手不存在");
        }
        Long adminId=ThreadLocalUtil.getUserId();

        Integer status=auth.getStatus();
        if (status.equals(2)){
            Artist artist = new Artist();
            artist.setArtistId(artistId);
            artist.setUserId(userId);
            artist.setStatus(status);
            artistMapper.updateById(artist);
        }

        ArtistAuth artistAuth = new ArtistAuth();
        BeanUtil.copyProperties(auth, artistAuth, "id");
        artistAuth.setAdminId(adminId);
        artistAuth.setCreateTime(LocalDateTime.now());
        artistAuth.setAuditTime(LocalDateTime.now());
        return baseMapper.insert(artistAuth) > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }

}
