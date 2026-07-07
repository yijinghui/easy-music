package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.exception.BaseException;
import com.easy.mapper.ArtistAuthMapper;
import com.easy.mapper.ArtistMapper;
import com.easy.mapper.SongMapper;
import com.easy.mapper.UserMapper;
import com.easy.pojo.dto.ArtistPageQueryDTO;
import com.easy.pojo.dto.ArtistDTO;
import com.easy.pojo.dto.SongDTO;
import com.easy.pojo.entity.Artist;
import com.easy.pojo.entity.ArtistAuth;
import com.easy.pojo.entity.User;
import com.easy.pojo.vo.ArtistNameVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.ArtistService;
import com.easy.utils.ThreadLocalUtil;
import com.minio.MinioTemplate;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Tag(name = "Admin端-歌手管理接口")
public class ArtistServiceImpl extends ServiceImpl<ArtistMapper, Artist> implements ArtistService {

    private final MinioTemplate minioTemplate;

    private final ArtistAuthMapper artistAuthMapper;

    private final UserMapper userMapper;
    private final SongMapper songMapper;


    @Override
    public PageResult page(ArtistPageQueryDTO pageQueryDTO) {

        IPage<Artist> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());

        Long artistId = pageQueryDTO.getArtistId();
        String name = pageQueryDTO.getArtistName();
        Integer gender = pageQueryDTO.getGender();
        String area = pageQueryDTO.getArea();
        Integer status = pageQueryDTO.getStatus();

        LambdaQueryWrapper<Artist> queryWrapper = new LambdaQueryWrapper<Artist>()
                .like(StrUtil.isNotBlank(name), Artist::getArtistName, name)
                .eq(artistId != null, Artist::getArtistId, artistId)
                .eq(gender != null, Artist::getGender, gender)
                .eq(status != null, Artist::getStatus, status)
                .eq(StrUtil.isNotBlank(area), Artist::getArea, area)
                .orderByDesc(Artist::getArtistId);

        IPage<Artist> result = page(page, queryWrapper);

        return new PageResult(result.getTotal(), result.getRecords());
    }



    @Override
    public Result addArtist(ArtistDTO artistDTO) {
        Artist artist = new Artist();
        BeanUtil.copyProperties(artistDTO, artist);
        return baseMapper.insert(artist) > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }

    @Override
    public Result updateArtist(ArtistDTO artistDTO) {
        Artist artist = new Artist();
        BeanUtil.copyProperties(artistDTO, artist);
        return baseMapper.updateById(artist) > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Override
    public Result updateArtistAvatar(Long artistId, MultipartFile avatar) {
        // 1.上传文件
        String avatarUrl = minioTemplate.uploadFile(avatar, "artists");

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
            minioTemplate.deleteFile(avatarUrl);
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
                minioTemplate.deleteFile(avatarUrl);
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

    @Override
    public Result<List<ArtistNameVO>> getAllArtistNames() {
        List<Artist> artists = baseMapper.selectList(new QueryWrapper<Artist>().orderByDesc("id"));
        if (artists.isEmpty()) {
            return Result.success("未找到相关数据", null);
        }

        List<ArtistNameVO> artistNameVOList = artists.stream()
                .map(artist -> {
                    ArtistNameVO artistNameVO = new ArtistNameVO();
                    artistNameVO.setArtistId(artist.getArtistId());
                    artistNameVO.setArtistName(artist.getArtistName());
                    return artistNameVO;
                }).toList();

        return Result.success(artistNameVOList);
    }

    @Override
    public Result updateArtistInfo(ArtistDTO artistDTO) {
        return null;
    }





    @Transactional(rollbackFor = Exception.class)
    public void certify(Long artistId) {
        Long userId = ThreadLocalUtil.getUserId();

        User user = userMapper.selectById(userId);

        // 1.检查歌手是否已认证
        if (user.getArtistId() != null) {
            throw new BaseException("用户已认证");
        }

         ArtistAuth artistAuth = new ArtistAuth();
         artistAuth.setUserId(userId);
         artistAuth.setArtistId(artistId);
         artistAuth.setStatus(0);
         artistAuth.setCreateTime(LocalDateTime.now());
         artistAuthMapper.insert(artistAuth);

    }



}
