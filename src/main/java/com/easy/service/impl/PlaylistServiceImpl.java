package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.mapper.*;
import com.easy.pojo.dto.*;
import com.easy.pojo.entity.*;
import com.easy.pojo.vo.PlaylistSongVO;
import com.easy.pojo.vo.PlaylistVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.MinIOService;
import com.easy.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist> implements PlaylistService{


    private final PlaylistSongMapper playlistSongMapper;

    private final PlaylistCommentMapper playlistCommentMapper;

    private final PlaylistLikeMapper playlistLikeMapper;

    private final UserMapper userMapper;

    private final MinIOService minIOService;

    private final SongMapper songMapper;

    @Override
    public Result<Long> getAllPlaylistsCount(String style) {

        QueryWrapper<Playlist>queryWrapper=new QueryWrapper<>();

        // 若style不为null则按style查询，否则全量查询
        if (style!=null){
            queryWrapper.eq("style",style);
        }

        Long count = baseMapper.selectCount(queryWrapper);

        return Result.success(count);
    }

    @Override
    public Result<PageResult<PlaylistVO>> getAllPlaylists(PlaylistPageQueryDTO pageQueryDTO) {

        Page<Playlist> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());

        String orderBy = pageQueryDTO.getOrderBy();
        String sortRule = pageQueryDTO.getSortRule();

        // 检测orderBy是否合法，防止SQL注入
        Set<String> orderBySet = Set.of("id", "likeCount", "commentCount", "playCount", "createTime", "updateTime");
        if (!orderBySet.contains(orderBy)||
                (!sortRule.equals("asc")&&!sortRule.equals("desc"))){
            return Result.error("操作不合法");
        }


        Page<PlaylistVO> playlistPage = baseMapper.selectPageWithPlaylistInfo(page, pageQueryDTO);

        if (playlistPage.getRecords().isEmpty()){
            return Result.success("未找到相关数据",new PageResult<>(0L,null));
        }

        List<PlaylistVO> playlistVOList = playlistPage.getRecords();


        PageResult<PlaylistVO> pageResult = new PageResult<>(playlistPage.getTotal(), playlistVOList);

        return Result.success(pageResult);
    }

    @Override
    public Result addPlaylist(PlaylistAddDTO playlistAddDTO) {

        String title = playlistAddDTO.getTitle();
        Long userId = playlistAddDTO.getUserId();


        if (userId == null){
            playlistAddDTO.setUserId(1L);
            // return Result.error("用户ID不能为空");
        }

        if (!StrUtil.isNotBlank(title)){
            return Result.error("歌单标题不能为空");
        }
        if (userMapper.selectCount(new QueryWrapper<User>().eq("id",userId))==0){
            return Result.error("用户不存在");
        }

        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title",title);
        queryWrapper.eq("user_id",userId);

        if (baseMapper.selectCount(queryWrapper)>0){
            return Result.error("歌单已存在");
        }

        Playlist playlist = new Playlist();
        BeanUtil.copyProperties(playlistAddDTO,playlist);

        if (baseMapper.insert(playlist)<=0){
            return Result.error("歌单添加失败");
        }

        return Result.success("添加歌单成功");
    }

    @Override
    public Result updatePlaylist(PlaylistUpdateDTO playlistUpdateDTO) {
        Long playlistId = playlistUpdateDTO.getPlaylistId();
        Long userId = playlistUpdateDTO.getUserId();

        if (playlistId == null){
            return Result.error("歌单ID不能为空");
        }

        if (userId == null){
            return Result.error("用户ID不能为空");
        }

        Playlist playlist = baseMapper.selectById(playlistId);

        if (playlist == null){
            return Result.error("歌单不存在");
        }

        BeanUtil.copyProperties(playlistUpdateDTO,playlist);
        playlist.setUpdateTime(LocalDateTime.now());

        if (baseMapper.updateById(playlist)<=0){
            return Result.error("歌单更新失败");
        }

        return Result.success("歌单更新成功");
    }

    @Override
    public Result updatePlaylistCover(Long playlistId, MultipartFile cover) {

        Playlist playlist = baseMapper.selectById(playlistId);
        if (playlist == null){
            return Result.error("歌单不存在");
        }
        String coverUrl = minIOService.uploadFile(cover, "playlists");

        playlist.setCoverUrl(coverUrl);
        baseMapper.updateById(playlist);

        return Result.success("歌单封面更新成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result deletePlaylist(Long playlistId) {

        if ((playlistId== null)){
            return Result.error("歌单ID不能为空");
        }
        Playlist playlist = baseMapper.selectById(playlistId);
        if (playlist == null){
            return Result.error("歌单不存在");
        }

        // 删除歌单封面
        if (StrUtil.isNotBlank(playlist.getCoverUrl())){
            minIOService.deleteFile(playlist.getCoverUrl());
        }

        // 删除歌单评论、点赞
        playlistLikeMapper.delete(new LambdaQueryWrapper<PlaylistLike>().eq(PlaylistLike::getPlaylistId, playlistId));
        playlistCommentMapper.delete(new LambdaQueryWrapper<PlaylistComment>().eq(PlaylistComment::getPlaylistId, playlistId));

        if (baseMapper.deleteById(playlistId)<=0){
            return Result.error("删除失败");
        }

        return Result.success("删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result deletePlaylists(List<Long> playlistIds) {
        if (playlistIds.isEmpty()){
            return Result.error("歌单ID不能为空");
        }

        List<Playlist> playlists = baseMapper.selectByIds(playlistIds);
        if (playlists.isEmpty()){
            return Result.error("删除失败，歌单不存在");
        }

        // 批量删除封面
        playlists.stream().filter(playlist -> StrUtil.isNotBlank(playlist.getCoverUrl())).forEach(playlist -> {
            minIOService.deleteFile(playlist.getCoverUrl());
        });

        // 批量删除点赞、评论
        playlistLikeMapper.delete(new LambdaQueryWrapper<PlaylistLike>().in(PlaylistLike::getPlaylistId, playlistIds));
        playlistCommentMapper.delete(new LambdaQueryWrapper<PlaylistComment>().in(PlaylistComment::getPlaylistId, playlistIds));

        if (baseMapper.deleteByIds(playlistIds)<=0){
            return Result.error("删除失败");
        }

        List<String> playlistTiles = playlists.stream().map(Playlist::getTitle).toList();

        return Result.success("已删除歌单"+playlistTiles);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result addPlaylistSongs(PlaylistSongAddDTO addDTO) {

        List<Long> songIds = addDTO.getSongIds();
        Long playlistId = addDTO.getPlaylistId();

        if (playlistId == null){
            return Result.error("歌单ID不能为空");
        }

        if (songIds.isEmpty()){
            return Result.error("请选择添加的歌曲");
        }

        List<Song> songs = songMapper.selectByIds(songIds);
        if (songs.isEmpty()){
            return Result.error("添加的歌曲不存在");
        }

        Playlist playlist = baseMapper.selectById(playlistId);
        if (playlist == null){
            return Result.error("歌单不存在");
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

        if (songList.isEmpty()){
            return Result.error("添加失败，歌曲已存在");
        }

        // 批量添加歌曲
        playlistSongMapper.insertBatch(songList);

        List<Long> addSongs = songList.stream().map(PlaylistSong::getSongId).toList();
        List<Song> songListDB = songMapper.selectByIds(addSongs);
        List<String> songNames = songListDB.stream().map(Song::getSongName).toList();

        return Result.success("已添加歌曲"+songNames);
    }

    @Override
    public Result<PageResult<PlaylistSongVO>> getPlaylistSongs(PlaylistSongPageQueryDTO pageQueryDTO) {
        Page<PlaylistSongVO> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        Long playlistId = pageQueryDTO.getPlaylistId();
        Page<PlaylistSongVO> playlistSongVOPage = playlistSongMapper.selectPageWithSongInfo(page,playlistId);


        if (playlistSongVOPage.getRecords().isEmpty()){
            return Result.success("未找到相关数据",new PageResult<>(0L,null));
        }

        return Result.success("查询成功",
                new PageResult<>(playlistSongVOPage.getTotal(),playlistSongVOPage.getRecords()));
    }

}
