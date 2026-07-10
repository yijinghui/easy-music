package com.easy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.constant.MessageConstant;
import com.easy.enumeration.FavoriteTypeEnum;
import com.easy.mapper.PlaylistMapper;
import com.easy.mapper.SongMapper;
import com.easy.mapper.UserFavoriteMapper;
import com.easy.mapper.UserMapper;
import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.entity.Playlist;
import com.easy.pojo.entity.Song;
import com.easy.pojo.entity.UserFavorite;
import com.easy.pojo.vo.PlaylistVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.UserFavoriteService;
import com.easy.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 用户收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    private final SongMapper songMapper;
    private final PlaylistMapper playlistMapper;

    @Override
    public PageResult getUserFavoriteSongs(Long userId, PageQueryDTO pageQueryDTO) {
        if (userId == null){
            userId = ThreadLocalUtil.getUserId();
        }

        Page<Song> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        IPage<Song> songPage = songMapper.getUserFavoriteSongs(page, userId);

        List<Song> songs = songPage.getRecords();

        if (songs == null) {
            return new PageResult(0L, null);
        }

        if (userId == null) {
            songs.forEach(song -> song.setIsFavorite(true));
        }else  {
            Set<Long> ids = baseMapper.getUserFavoriteSongIds(ThreadLocalUtil.getUserId());
            songs.forEach(song -> song.setIsFavorite(ids.contains(song.getSongId())));
        }
        return new PageResult(songPage.getTotal(), songs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectSong(Long songId) {
        Long userId = ThreadLocalUtil.getUserId();

        // 查询用户收藏的歌曲
        Set<Long> ids = baseMapper.getUserFavoriteSongIds(userId);
        if (ids.contains(songId)) {
            throw new IllegalArgumentException("歌曲已收藏");
        }

        UserFavorite userFavorite = new UserFavorite()
                .setUserId(userId)
                .setType(FavoriteTypeEnum.SONG.getId())
                .setSongId(songId)
                .setCreateTime(LocalDateTime.now());
        save(userFavorite);

        // 更新歌曲收藏数
        LambdaUpdateWrapper<Song> luw = new LambdaUpdateWrapper<Song>()
                .eq(Song::getSongId, songId)
                .setSql("favorite_count = favorite_count + 1");
        songMapper.update(null, luw);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCollectSong(Long songId) {
        Long userId = ThreadLocalUtil.getUserId();

        LambdaQueryWrapper<UserFavorite> queryWrapper = new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getSongId, songId);
        remove(queryWrapper);

        // 更新歌曲收藏数
        LambdaUpdateWrapper<Song> luw = new LambdaUpdateWrapper<Song>()
                .eq(Song::getSongId, songId)
                .setSql("favorite_count = favorite_count - 1");
        songMapper.update(null, luw);

    }

    @Override
    public PageResult getUserFavoritePlaylists(Long userId, PageQueryDTO pageQueryDTO) {
        if (userId == null){
            userId = ThreadLocalUtil.getUserId();
        }
        Page<PlaylistVO> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        Page<PlaylistVO> playlistPage = playlistMapper.getUserFavoritePlaylists(page, userId);

        if (playlistPage.getRecords().isEmpty()) {
            return  new PageResult(0L, null);
        }
        return new PageResult(playlistPage.getTotal(), playlistPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectPlaylist(Long playlistId) {
        Long userId = ThreadLocalUtil.getUserId();


        UserFavorite userFavorite = new UserFavorite()
                .setUserId(userId)
                .setType(FavoriteTypeEnum.PLAYLIST.getId())
                .setPlaylistId(playlistId)
                .setCreateTime(LocalDateTime.now());
        save(userFavorite);

        // 更新歌单收藏数
        LambdaUpdateWrapper<Playlist> luw = new LambdaUpdateWrapper<Playlist>()
                .eq(Playlist::getPlaylistId, playlistId)
                .setSql("like_count = like_count + 1");
        playlistMapper.update(null, luw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCollectPlaylist(Long playlistId) {
        Long userId = ThreadLocalUtil.getUserId();

        LambdaQueryWrapper<UserFavorite> queryWrapper = new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getPlaylistId, playlistId);
        remove(queryWrapper);

        // 更新歌单收藏数
        LambdaUpdateWrapper<Playlist> luw = new LambdaUpdateWrapper<Playlist>()
                .eq(Playlist::getPlaylistId, playlistId)
                .setSql("like_count = like_count - 1");
        playlistMapper.update(null, luw);
    }




}
