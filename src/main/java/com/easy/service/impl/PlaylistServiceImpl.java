package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.exception.AccessDeniedException;
import com.easy.exception.BaseException;
import com.easy.mapper.*;
import com.easy.pojo.dto.*;
import com.easy.pojo.entity.*;
import com.easy.pojo.vo.PlaylistVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.PlaylistService;
import com.easy.utils.ThreadLocalUtil;
import com.minio.MinioTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.easy.constant.MessageConstant.ACCESS_DENIED;


@Slf4j
@RequiredArgsConstructor
@Service
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist> implements PlaylistService {


    private final PlaylistSongMapper playlistSongMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final MinioTemplate minioTemplate;


    @Override
    public PageResult list(PlaylistPageQueryDTO pageQueryDTO) {

        Page<Playlist> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        Page<PlaylistVO> playlistPage = baseMapper.selectPageWithUsername(page, pageQueryDTO);
        if (playlistPage.getRecords().isEmpty()) {
            return new PageResult(0L, null);
        }
        return new PageResult(playlistPage.getTotal(), playlistPage.getRecords());
    }

    @Override
    public void create(PlaylistDTO playlistDTO) {
        Playlist playlist = new Playlist();
        playlist.setUserId(ThreadLocalUtil.getUserId());
        playlist.setTitle(playlistDTO.getTitle());
        playlist.setIntroduction(playlistDTO.getIntroduction());
        playlist.setStyle(playlistDTO.getStyle());
        playlist.setCreateTime(LocalDateTime.now());
        save(playlist);
    }




    @Override
    public void update(PlaylistDTO playlistDTO) {
        Long playlistId = playlistDTO.getPlaylistId();
        Long userId = ThreadLocalUtil.getUserId();

        Playlist playlist = getById(playlistId);
        if (!playlist.getUserId().equals(userId)) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }

        BeanUtil.copyProperties(playlistDTO, playlist);
        playlist.setUpdateTime(LocalDateTime.now());
        updateById(playlist);
    }

    @Override
    public PageResult search(String text, PageQueryDTO pageQueryDTO) throws IOException {
        Page<Playlist> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        Page<Playlist> result = lambdaQuery().like(Playlist::getTitle, text)
                .page(page);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public PlaylistVO getDetailById(Long playlistId) {
        // 获取用户收藏歌单列表
        Long userId = ThreadLocalUtil.getUserId();
        Set<Long> ids = userFavoriteMapper.getUserFavoritePlaylistIds(userId);
        PlaylistVO playlistVO = baseMapper.selectDetailById(playlistId);
        if (ids.contains(playlistId)) {
            playlistVO.setIsFavorite(true);
        }
        return playlistVO;
    }

    @Override
    public void updateCover(Long playlistId, MultipartFile cover) {
        Playlist playlist = baseMapper.selectById(playlistId);
        if (!playlist.getUserId().equals(ThreadLocalUtil.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        // 更新歌单封面
        String oldCover = playlist.getCoverUrl();
        if (oldCover != null && !oldCover.equals("playlists/playlist_default.webp")) {
            minioTemplate.deleteFile(oldCover);
        }
        // 上传新封面
        String coverUrl = minioTemplate.uploadFile(cover, "playlists");
        playlist.setCoverUrl(coverUrl);
        updateById(playlist);
    }







    @Transactional(rollbackFor = Exception.class)
    public void addSongs(Long playlistId, List<Long> songIds) {

        if (playlistId == null) {
            throw new IllegalArgumentException("歌单ID不能为空");
        }

        if (songIds.isEmpty()) {
            throw new IllegalArgumentException("请选择添加的歌曲");
        }


        // 查询歌单中已有的歌单，防止重复添加
        LambdaQueryWrapper<PlaylistSong> queryWrapper = new LambdaQueryWrapper<PlaylistSong>()
                .eq(PlaylistSong::getPlaylistId, playlistId);
        List<PlaylistSong> existSongs = playlistSongMapper.selectList(queryWrapper);
        Set<Long> existSongIds = existSongs.stream().map(PlaylistSong::getSongId).collect(Collectors.toSet());

        List<PlaylistSong> songList = songIds.stream().filter(songId -> !existSongIds.contains(songId)).map(songId -> {
            PlaylistSong playlistSong = new PlaylistSong();
            playlistSong.setPlaylistId(playlistId);
            playlistSong.setSongId(songId);
            return playlistSong;
        }).toList();

        if (songList.isEmpty()) {
            throw new BaseException("添加失败，歌曲已存在");
        }

        // 批量添加歌曲
        playlistSongMapper.insertBatch(songList);
    }





    @Override
    public void deleteSongs(Long playlistId, List<Long> songIds) {
        playlistSongMapper.delete(new LambdaQueryWrapper<PlaylistSong>()
                .eq(PlaylistSong::getPlaylistId, playlistId)
                .in(PlaylistSong::getSongId, songIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long playlistId) {
        Long userId = ThreadLocalUtil.getUserId();

        Playlist playlist = getById(playlistId);

        if (!userId.equals(playlist.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }

        removeById(playlistId);

        // 删除歌单-歌曲关联
        playlistSongMapper.delete(new LambdaQueryWrapper<PlaylistSong>()
                .eq(PlaylistSong::getPlaylistId, playlistId));

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSong(Long playlistId, Long songId) {
        Long userId = ThreadLocalUtil.getUserId();

        Playlist playlist = getById(playlistId);

        if (!userId.equals(playlist.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }

        // 检查歌曲是否已存在歌单中
        PlaylistSong playlistSong = playlistSongMapper.selectOne(new LambdaQueryWrapper<PlaylistSong>()
                .eq(PlaylistSong::getPlaylistId, playlistId)
                .eq(PlaylistSong::getSongId, songId));
        if (playlistSong != null) {
            throw new BaseException("歌曲已存在");
        }
        playlistSongMapper.insert(new PlaylistSong(playlistId, songId));
    }

    @Override
    public void removeSong(Long playlistId, Long songId) {
        Long userId = ThreadLocalUtil.getUserId();
        Playlist playlist = baseMapper.selectById(playlistId);

        if (!userId.equals(playlist.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }

        playlistSongMapper.delete(new LambdaUpdateWrapper<PlaylistSong>()
                .eq(PlaylistSong::getPlaylistId, playlistId)
                .eq(PlaylistSong::getSongId, songId));
    }

    @Override
    public List<Playlist> listByUserId(Long userId) {
        QueryWrapper<Playlist> qw = new QueryWrapper<>();
        qw.lambda().eq(Playlist::getUserId, userId)
                .orderByDesc(Playlist::getCreateTime);
        return list(qw);
    }

}
