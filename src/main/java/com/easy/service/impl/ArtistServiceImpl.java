package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.entity.Artist;
import com.easy.pojo.entity.ArtistAuth;
import com.easy.pojo.entity.User;
import com.easy.result.PageResult;
import com.easy.service.ArtistService;
import com.easy.utils.ThreadLocalUtil;
import com.minio.MinioTemplate;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public void add(ArtistDTO artistDTO) {
        Artist artist = new Artist();
        BeanUtil.copyProperties(artistDTO, artist);
        save(artist);
    }



    @Override
    public void update(ArtistDTO artistDTO) {
        Artist artist = new Artist();
        BeanUtil.copyProperties(artistDTO, artist);
        baseMapper.updateById(artist);
    }



    @Override
    public void updateAvatar(Long artistId, MultipartFile avatar) {
        // 1.上传文件
        String avatarUrl = minioTemplate.uploadFile(avatar, "artists");

        // 2，检查文件是否上传成功
        if (avatarUrl == null) {
            throw new BaseException("上传头像失败");
        }
        // 3.更新数据库
        lambdaUpdate().eq(Artist::getArtistId, artistId)
                .set(Artist::getAvatar, avatarUrl)
                .set(Artist::getUpdateTime, LocalDateTime.now())
                .update();

    }

    @Override
    public void delete(Long artistId) {
        // 1.检查歌手是否存在
        Artist artist = getById(artistId);
        // 2.删除歌手头像
        String avatarUrl = artist.getAvatar();
        if (StrUtil.isNotBlank(avatarUrl)) {
            minioTemplate.deleteFile(avatarUrl);
        }
        // 3.删除歌手
        removeById(artistId);

    }

    @Override
    public void deleteByIds(List<Long> artistIds) {
        // 查询歌手信息
        List<Artist> artists = listByIds(artistIds);
        if (artists.isEmpty()){
            return;
        }
        for (Artist artist : artists) {
            // 删除歌手头像
            String avatarUrl = artist.getAvatar();
            if (StrUtil.isNotBlank(avatarUrl)) {
                minioTemplate.deleteFile(avatarUrl);
            }
        }
        removeByIds(artistIds);
    }

    @Override
    public Long getCount(Integer gender, String area) {
        LambdaQueryWrapper<Artist> queryWrapper = new LambdaQueryWrapper<Artist>()
                .eq(gender != null, Artist::getGender, gender)
                .eq(StrUtil.isNotBlank(area), Artist::getArea, area);
        return baseMapper.selectCount(queryWrapper);
    }



    @Override
    public List<Map<String,Object>> getNames() {
        List<Artist> artists = baseMapper.selectList(new QueryWrapper<Artist>().orderByDesc("id"));
        if (artists.isEmpty()) {
            return new ArrayList<>();
        }

        return artists.stream()
                .map(artist -> {
                    Map<String,Object> map = new HashMap<>();
                    map.put("artistId", artist.getArtistId());
                    map.put("artistName", artist.getArtistName());
                    return map;
                }).toList();
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

    @Override
    public PageResult search(String artistName, PageQueryDTO pageQueryDTO) {
        Page<Artist> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        Page<Artist> result = lambdaQuery().like(Artist::getArtistName, artistName)
                .page(page);
        return new PageResult(result.getTotal(), result.getRecords());
    }


}
