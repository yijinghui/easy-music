package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
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
import com.easy.utils.EsClientUtil;
import com.easy.utils.ThreadLocalUtil;
import com.minio.MinioTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.easy.constant.MessageConstant.ACCESS_DENIED;


@Slf4j
@RequiredArgsConstructor
@Service
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist> implements PlaylistService{


    private final PlaylistSongMapper playlistSongMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final StringRedisTemplate stringRedisTemplate;


    private final UserMapper userMapper;

    private final MinioTemplate minioTemplate;

    private final SongMapper songMapper;

    private final CommentMapper commentMapper;

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
    public Result<PageResult> getAllPlaylists(PlaylistPageQueryDTO pageQueryDTO) {

        Page<Playlist> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());



        Page<PlaylistVO> playlistPage = baseMapper.selectPageWithPlaylistInfo(page, pageQueryDTO);

        if (playlistPage.getRecords().isEmpty()){
            return Result.success("未找到相关数据",new PageResult(0L,null));
        }

        List<PlaylistVO> playlistVOList = playlistPage.getRecords();


        PageResult pageResult = new PageResult(playlistPage.getTotal(), playlistVOList);

        return Result.success(pageResult);
    }

    @Override
    public void create(PlaylistDTO playlistDTO) {

        Playlist playlist = new Playlist();
        playlist.setUserId(ThreadLocalUtil.getUserId());
        BeanUtil.copyProperties(playlistDTO,playlist,
                "playlistId");

        save(playlist);
    }

    @Override
    public Result updatePlaylist(PlaylistDTO playlistDTO) {
        return null;
    }

    @Override
    public void update(PlaylistDTO playlistDTO) {
        Long playlistId = playlistDTO.getPlaylistId();
        Long userId = ThreadLocalUtil.getUserId();

        Playlist playlist = getById(playlistId);
        if(!playlist.getUserId().equals(userId)){
            throw new AccessDeniedException(ACCESS_DENIED);
        }

        BeanUtil.copyProperties(playlistDTO,playlist);
        playlist.setUpdateTime(LocalDateTime.now());
        updateById(playlist);
    }

    @Override
    public PageResult search(String text) throws IOException {
        ElasticsearchClient client = EsClientUtil.getEsClient();

        // 获取用户收藏歌曲列表
        Long userId = ThreadLocalUtil.getUserId();
        Set<Long> ids = userFavoriteMapper.getUserFavoritePlaylistIds(userId);

        SearchResponse<PlaylistVO> response = client.search(s -> s
                        .index("playlist")
                        .query(q -> q
                                .multiMatch(m -> m
                                        .fields("title", "style", "userName")
                                        .query(text)
                                        .type(TextQueryType.Phrase)
                                )
                        )
                        .highlight(h -> h
                                .fields("title", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                                .fields("style", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                        )
                        .size(10),  // 返回前10条
                PlaylistVO.class
        );

        List<PlaylistVO> result=new ArrayList<>();
        long total=0L;

        // 高亮处理
        if (response != null) {
            total = response.hits().total().value();
            log.info("共查询到{}条记录", total);

            List<Hit<PlaylistVO>> hits = response.hits().hits();
            for (Hit<PlaylistVO> hit : hits) {
                Map<String, List<String>> highlight = hit.highlight();

                PlaylistVO source = hit.source();

                if (highlight.containsKey("title")) {
                    source.setTitle(highlight.get("title").get(0));
                }
                if (highlight.containsKey("style")) {
                    source.setStyle(highlight.get("style").get(0));
                }

                if(ids.contains(source.getPlaylistId())) {
                    source.setIsFavorite(true);
                }

                log.info("处理后的文本：{}", source.toString());
                result.add(source);

            }
        }
        return new PageResult(total,result);
    }

    @Override
    public PlaylistVO getDetailById(Long playlistId) {
        // 获取用户收藏歌单列表
        Long userId = ThreadLocalUtil.getUserId();
        Set<Long> ids = userFavoriteMapper.getUserFavoritePlaylistIds(userId);
        PlaylistVO playlistVO = baseMapper.selectDetailById(playlistId);
        if(ids.contains(playlistId)) {
            playlistVO.setIsFavorite(true);
        }
        return playlistVO;
    }


    @Override
    public Result updatePlaylistCover(Long playlistId, MultipartFile cover) {

        Playlist playlist = baseMapper.selectById(playlistId);
        if (playlist == null){
            return Result.error("歌单不存在");
        }
        String coverUrl = minioTemplate.uploadFile(cover, "playlists");

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

        if (playlist == null) {
            return Result.error("歌单不存在");
        }

        Long userId = playlist.getUserId();

        // 删除歌单封面
        if (StrUtil.isNotBlank(playlist.getCoverUrl())){
            minioTemplate.deleteFile(playlist.getCoverUrl());
        }

        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getPlaylistId, playlistId));

        playlistSongMapper.delete(new LambdaQueryWrapper<PlaylistSong>().eq(PlaylistSong::getPlaylistId, playlistId));

        if (baseMapper.deleteById(playlistId)==0){
            return Result.error("删除失败，歌单不存在");
        }

        return Result.success("已删除歌单"+playlist.getTitle());
    }

    @Transactional(rollbackFor = Exception.class)
    public Result deletePlaylists(List<Long> playlistIds) {
        if (playlistIds.isEmpty()){
            return Result.error("歌单ID不能为空");
        }

        List<Playlist> playlists = baseMapper.selectByIds(playlistIds);

        // 批量删除封面
        playlists.stream().filter(playlist -> StrUtil.isNotBlank(playlist.getCoverUrl())).forEach(playlist -> {
            minioTemplate.deleteFile(playlist.getCoverUrl());
        });

        commentMapper.delete(new LambdaQueryWrapper<Comment>().in(Comment::getPlaylistId, playlistIds));

        if (baseMapper.deleteByIds(playlistIds)==0){
            return Result.error("删除失败，歌单不存在");
        }

        List<String> playlistTiles = playlists.stream().map(Playlist::getTitle).toList();

        return Result.success("已删除歌单"+playlistTiles);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result addPlaylistSongs(Long playlistId, List<Long> songIds) {

        if (playlistId == null){
            return Result.error("歌单ID不能为空");
        }

        if (songIds.isEmpty()){
            return Result.error("请选择添加的歌曲");
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

        return Result.success("添加成功");
    }



    @Override
    public List<Playlist> getPlaylistInfo(Long userId) {
        if (userId == null) {
            return List.of();
        }

        // 查询用户创建的歌单列表
        LambdaQueryWrapper<Playlist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Playlist::getUserId, userId);
        queryWrapper.orderByDesc(Playlist::getCreateTime);
        List<Playlist> playlists = baseMapper.selectList(queryWrapper);

        if (playlists.isEmpty()) {
            return List.of();
        }

        return playlists;
    }

    @Override
    public Result<PageResult> getSongsByPlaylistId(Long playlistId, PageQueryDTO pageQueryDTO) {

        if(playlistId==null) return Result.error("歌单ID不能为空",new PageResult(0L, null));
        Page<Song> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());


        IPage<Song> songPage = songMapper.getSongsByPlaylistId(page, playlistId);

        if (songPage.getRecords().isEmpty()) {
            return Result.success("未找到相关数据", new PageResult(0L, null));
        }
        return Result.success(new PageResult(songPage.getTotal(), songPage.getRecords()));
    }

    @Override
    public Result deletePlaylistSongs(Long playlistId,List<Long> songIds) {
        int delete = playlistSongMapper.delete(new LambdaQueryWrapper<PlaylistSong>().eq(PlaylistSong::getPlaylistId, playlistId).in(PlaylistSong::getSongId, songIds));
        return delete==0?Result.error("批量删除失败"):Result.success("批量删除成功");

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
    public Result updatePlaylistByUser(PlaylistDTO playlistDTO, Long userId) {
        if (playlistDTO.getPlaylistId() == null || userId == null) {
            return Result.error("参数不能为空");
        }

        Playlist playlist = baseMapper.selectById(playlistDTO.getPlaylistId());
        if (playlist == null) {
            return Result.error("歌单不存在");
        }

        if (!userId.equals(playlist.getUserId())) {
            return Result.error("无权操作该歌单");
        }
        return updatePlaylist(playlistDTO);
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
