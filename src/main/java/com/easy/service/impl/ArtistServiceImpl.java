package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.mapper.ArtistMapper;
import com.easy.pojo.dto.ArtistAddDTO;
import com.easy.pojo.dto.ArtistPageQueryDTO;
import com.easy.pojo.dto.ArtistUpdateDTO;
import com.easy.pojo.entity.Artist;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.ArtistService;
import com.easy.service.MinIOService;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ArtistServiceImpl extends ServiceImpl<ArtistMapper, Artist> implements ArtistService {

    private final MinIOService minIOService;

    @Override
    public Result<PageResult<Artist>> page(ArtistPageQueryDTO pageQueryDTO) {
        IPage<Artist> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        String name = pageQueryDTO.getArtistName();
        Integer gender = pageQueryDTO.getGender();
        String area = pageQueryDTO.getArea();

        LambdaQueryWrapper<Artist> queryWrapper = new LambdaQueryWrapper<Artist>()
                .like(StrUtil.isNotBlank(name), Artist::getArtistName, name)
                .eq(gender != null, Artist::getGender, gender)
                .like(StrUtil.isNotBlank(area), Artist::getArea, area);

        IPage<Artist> result = baseMapper.selectPage(page, queryWrapper);

        if (result.getTotal() == 0) {
            return Result.success("未找到相关数据", new PageResult<>(0L, null));
        }

        return Result.success(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @Override
    public Result addArtist(ArtistAddDTO artistAddDTO) {
        Artist artist = new Artist();
        BeanUtil.copyProperties(artistAddDTO, artist);
        artist.setCreateTime(LocalDateTime.now());
        artist.setUpdateTime(LocalDateTime.now());
        return baseMapper.insert(artist) > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }

    @Override
    public Result updateArtist(ArtistUpdateDTO artistUpdateDTO) {
        Long id= artistUpdateDTO.getArtistId();
        Artist artist = baseMapper.selectById(id);
        if (artist == null) {
            return Result.error("歌手不存在");
        }
        String artistName = artistUpdateDTO.getArtistName();
        Integer gender = artistUpdateDTO.getGender();
        LocalDate birth = artistUpdateDTO.getBirth();
        String area = artistUpdateDTO.getArea();
        String introduction = artistUpdateDTO.getIntroduction();
        LambdaUpdateWrapper<Artist> updateWrapper = new LambdaUpdateWrapper<Artist>()
                .eq(Artist::getArtistId, id)
                .set(StrUtil.isNotBlank(artistName), Artist::getArtistName, artistName)
                .set(gender != null, Artist::getGender, gender)
                .set(birth != null, Artist::getBirth, birth)
                .set(StrUtil.isNotBlank(area), Artist::getArea, area)
                .set(StrUtil.isNotBlank(introduction), Artist::getIntroduction, introduction)
                .set(Artist::getUpdateTime, LocalDateTime.now());
        return baseMapper.update(null, updateWrapper) > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Override
    public Result updateArtistAvatar(Long artistId, MultipartFile avatar) {
        // 1.上传文件
        String avatarUrl = minIOService.uploadFile(avatar, "artists");

        // 2，检查文件是否上传成功
        if (avatarUrl == null) {
            return Result.error("上传头像失败");
        }
        // 3.更新数据库
        LambdaUpdateWrapper<Artist> updateWrapper = new LambdaUpdateWrapper<Artist>()
                .eq(Artist::getArtistId, artistId)
                .set(Artist::getAvatar, avatarUrl)
                .set(Artist::getUpdateTime, LocalDateTime.now());

        // 4.返回结果
        return baseMapper.update(null, updateWrapper) > 0 ? Result.success("更新头像成功") : Result.error("更新头像失败");

    }

    @Override
    public Result deleteArtist(Long artistId) {
        // 1.检查歌手是否存在
        Artist artist = baseMapper.selectById(artistId);
        if (artist == null) {
            return Result.error("歌手不存在,删除失败");
        }
        // 2.删除歌手头像
        String avatarUrl = artist.getAvatar();
        if (StrUtil.isNotBlank(avatarUrl)) {
            minIOService.deleteFile(avatarUrl);
        }
        // 3.删除歌手
        return baseMapper.deleteById(artistId) > 0 ? Result.success("删除成功") : Result.error("删除失败");

    }

    @Override
    public Result deleteArtists(List<Long> artistIds) {
        // 查询歌手信息
        List<Artist> artists = baseMapper.selectByIds(artistIds);
        if (artists.isEmpty()){
            return Result.error("歌手不存在,删除失败");
        }
        for (Artist artist : artists) {
            // 删除歌手头像
            String avatarUrl = artist.getAvatar();
            if (StrUtil.isNotBlank(avatarUrl)) {
                minIOService.deleteFile(avatarUrl);
            }
        }

        return baseMapper.deleteByIds(artistIds) > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }

    @Override
    public Result<Long> getAllArtistsCount(Integer gender, String area) {
        LambdaQueryWrapper<Artist> queryWrapper = new LambdaQueryWrapper<Artist>()
                .eq(gender != null, Artist::getGender, gender)
                .eq(StrUtil.isNotBlank(area), Artist::getArea, area);
        return Result.success(baseMapper.selectCount(queryWrapper));
    }


}
