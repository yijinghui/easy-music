package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
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
import com.easy.mapper.*;
import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.dto.SongDTO;
import com.easy.pojo.dto.SongPageQueryDTO;
import com.easy.pojo.entity.*;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.SongService;
import com.easy.utils.EsClientUtil;
import com.easy.utils.ThreadLocalUtil;
import com.minio.MinioTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.easy.constant.MessageConstant.ACCESS_DENIED;


@Service
@RequiredArgsConstructor
@Slf4j
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    private static final int RECOMMEND_POOL_SIZE = 80;
    private static final String RECOMMEND_REDIS_KEY_PREFIX = "recommended_songs:";

    private final StyleMapper styleMapper;
    private final UserMapper userMapper;

    private final GenreMapper genreMapper;

    private final UserFavoriteMapper userFavoriteMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final MinioTemplate minIOService;
    private final PlayRecordMapper playRecordMapper;
    private final PlaylistMapper playlistMapper;
    private final ArtistMapper artistMapper;

    @Override
    public Result<Long> getAllSongsCount(String style) {

        LambdaQueryWrapper<Song> queryWrapper = new LambdaQueryWrapper<Song>()
                .like(Song::getStyle, style);

        return Result.success(baseMapper.selectCount(queryWrapper));
    }

    @Override
    public Result<PageResult> getAllSongs(SongPageQueryDTO pageQueryDTO) {

        Page<Song> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        String style = pageQueryDTO.getStyle();
        Long artistId = pageQueryDTO.getArtistId();
        String songName = pageQueryDTO.getSongName();
        String album = pageQueryDTO.getAlbum();
        Long id = pageQueryDTO.getId();
        IPage<Song> songPage = baseMapper.getAllSongs(page, artistId, style, songName, id, album);

        if (songPage.getRecords().isEmpty()) {
            return Result.success("未找到相关数据", new PageResult(0L, null));
        }

        return Result.success(new PageResult(songPage.getTotal(), songPage.getRecords()));
    }


    @Transactional(rollbackFor = Exception.class)
    public Result addSong(SongDTO songDTO) {

        Song song = new Song();
        BeanUtil.copyProperties(songDTO, song, "songId");
        baseMapper.insert(song);

        Long songId = song.getSongId();
        String style = song.getStyle();

        // 获取歌曲所有风格
        handleStyleUpdate(style, songId);

        return Result.success("添加成功");

    }


    @Transactional(rollbackFor = Exception.class)
    public Result updateSong(SongDTO songDTO) {
        Song song = new Song();
        BeanUtil.copyProperties(songDTO, song);

        // 查询歌曲是否存在
        Song songDB = baseMapper.selectById(song.getSongId());
        if (songDB == null) {
            return Result.error("歌曲不存在");
        }

        String oldStyle = songDB.getStyle();
        String newStyle = song.getStyle();
        Long songId = song.getSongId();

        BeanUtil.copyProperties(song, songDB);

        baseMapper.updateById(songDB);

        if (newStyle == null) {
            // 删除歌曲-风格
            genreMapper.delete(new QueryWrapper<Genre>().eq("song_id", songId));
            return Result.success("更新成功");
        }

        if (oldStyle.equals(newStyle)) {
            return Result.success("更新成功");
        }

        genreMapper.delete(new QueryWrapper<Genre>().eq("song_id", songId));
        handleStyleUpdate(newStyle, songId);
        return Result.success("更新成功");

    }

    @Transactional(rollbackFor = Exception.class)
    public Result updateSongCover(Long songId, MultipartFile cover) {
        String coverUrl = minIOService.uploadFile(cover, "songCovers");

        // 查询目标歌曲是否存在
        Song songDB = baseMapper.selectById(songId);
        if (songDB == null) {
            return Result.error("歌曲不存在");
        }
        songDB.setCoverUrl(coverUrl);
        baseMapper.updateById(songDB);

        return Result.success("更新成功");

    }

    @Override
    public Result updateSongAudio(Long songId, MultipartFile audio, String duration) {
        String audioUrl = minIOService.uploadFile(audio, "songAudios");

        // 查询目标歌曲是否存在
        Song song = new Song();
        song.setSongId(songId);
        song.setAudioUrl(audioUrl);
        song.setDuration(duration);
        if (baseMapper.updateById(song) == 0) {
            return Result.error("歌曲不存在");
        }
        ;

        return Result.success("已更新歌曲" + songId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result deleteSong(Long songId) {

        genreMapper.delete(new QueryWrapper<Genre>().eq("song_id", songId));

        Song songDB = baseMapper.selectById(songId);

        // 删除歌曲、歌曲封面
        handleCoverAndAudioDelete(songDB);


        if (baseMapper.deleteById(songId) == 0) {
            return Result.error("歌曲不存在");
        }

        return Result.success("已删除歌曲" + songDB.getSongName());
    }

    @Override
    public Result deleteSongs(List<Long> songIds) {

        List<Song> songList = baseMapper.selectByIds(songIds);
        List<String> songNames = songList.stream().map(Song::getSongName).toList();
        for (Song song : songList) {
            handleCoverAndAudioDelete(song);
        }
        if (baseMapper.delete(new QueryWrapper<Song>().in("song_id", songIds)) == 0) {
            return Result.error("歌曲不存在");
        }
        return Result.success("已删除歌曲" + songNames);


    }


    @Override
    public List<Song> getRecommendedSongs() {
        Long userId = ThreadLocalUtil.getUserId();
        List<Song> recommendSongs;
        if (userId == null || userId <= 0) {
            return baseMapper.getRandomSongs();
        }

        recommendSongs=baseMapper.getRandomSongs();
        Set<Long> ids = userFavoriteMapper.getUserFavoriteSongIds(userId);
        recommendSongs.forEach(song -> {
            if(ids.contains(song.getSongId())) {
                song.setIsFavorite(true);
            }
        });

        return  recommendSongs;
    }

    @Override
    public List<Song> getTop200SongsByMonth(Integer offset) {

        Long userId = ThreadLocalUtil.getUserId();
        // 查询用户是否收藏歌曲
        Set<Long> ids = userFavoriteMapper.getUserFavoriteSongIds(userId);

        String key;
        if(offset==0){
            key="top:monthly:now";
        }else{
            LocalDate lastMonth = LocalDate.now().minusMonths(offset);
            // 获取当前年份
            int year = lastMonth.getYear();
            // 获取当前月
            int month = lastMonth.getMonthValue();
            key="top:monthly:" + year + "-" + month;
        }

        List<String> top200IdsStr = new ArrayList<>();
        List<Long> top200Scores = new ArrayList<>();

        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 199);
        if (tuples != null && !tuples.isEmpty()) {
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                top200IdsStr.add(tuple.getValue());
                top200Scores.add(Math.round(tuple.getScore()));
            }
        }

        List<Long> top200Ids = top200IdsStr.stream().map(Long::valueOf).toList();
        // 查询歌曲信息
        List<Song> top200Songs = baseMapper.getByIds(top200Ids);

        for (int i = 0; i < top200Songs.size(); i++) {
            Song song = top200Songs.get(i);
            if (ids.contains(song.getSongId())) {
                song.setIsFavorite(true);
            }
            song.setPlayCount(top200Scores.get(i));
        }
        return top200Songs;

    }

    @Override
    public List<Song> getTop200SongsByWeek(Integer offset) {

        Long userId = ThreadLocalUtil.getUserId();
        // 查询用户是否收藏歌曲
        Set<Long> ids = userFavoriteMapper.getUserFavoriteSongIds(userId);

        String key;
        if(offset==0){
            key="top:weekly:now";
        }else{
            LocalDate lastMonth = LocalDate.now().minusWeeks(offset);
            // 获取当前年份
            int year = lastMonth.getYear();
            // 获取当前月
            int week = lastMonth.get(WeekFields.ISO.weekOfWeekBasedYear());;
            key="top:weekly:" + year + "-" + week;
        }
        List<String> top200IdsStr = new ArrayList<>();
        List<Long> top200Scores = new ArrayList<>();

        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 199);
        if (tuples != null && !tuples.isEmpty()) {
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                top200IdsStr.add(tuple.getValue());
                top200Scores.add(Math.round(tuple.getScore()));
            }
        }

        List<Long> top200Ids = top200IdsStr.stream().map(Long::valueOf).toList();
        // 查询歌曲信息
        List<Song> top200Songs = baseMapper.getByIds(top200Ids);

        for (int i = 0; i < top200Songs.size(); i++) {
            Song song = top200Songs.get(i);
            if (ids.contains(song.getSongId())) {
                song.setIsFavorite(true);
            }
            song.setPlayCount(top200Scores.get(i));
        }
        return top200Songs;

    }

    @Transactional(rollbackFor = Exception.class)
    public void listen(Long songId, Long playListId) {
        Long userId = ThreadLocalUtil.getUserId();

        // 更新播放记录表：
        PlayRecord pr = new PlayRecord();
        pr.setUserId(userId);
        pr.setPlaylistId(playListId);
        pr.setSongId(songId);
        pr.setCreateTime(LocalDateTime.now());
        playRecordMapper.insert(pr);

        // 若歌单id不为null，则说明歌曲是通过歌单去听取的
        if (playListId != null) {
            playlistMapper.update(null,
                    new LambdaUpdateWrapper<Playlist>()
                            .setSql("play_count = play_count + 1")
                            .eq(Playlist::getPlaylistId, playListId)
            );
        }

        baseMapper.update(null,
                new LambdaUpdateWrapper<Song>()
                        .setSql("play_count = play_count + 1")
                        .eq(Song::getSongId, songId)
        );

        // 更新排行榜

        stringRedisTemplate.opsForZSet().incrementScore(
                "top:monthly:now", songId.toString(), 1);
        stringRedisTemplate.opsForZSet().incrementScore(
                "top:weekly:now", songId.toString(), 1);

    }

    @Override
    public PageResult listSongByPlaylistId(Long playlistId, PageQueryDTO pageQueryDTO) {
        Page<Song> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        IPage<Song> songPage = baseMapper.getSongsByPlaylistId(page, playlistId);
        // 设置用户是否收藏
        Long userId = ThreadLocalUtil.getUserId();
        List<Song> songs = songPage.getRecords();
        Set<Long> ids = userFavoriteMapper.getUserFavoriteSongIds(userId);
        songs.forEach(song -> {
            if(ids.contains(song.getSongId())) {
                song.setIsFavorite(true);
            }
        });
        return new PageResult(songPage.getTotal(), songs);
    }

    @Override
    public PageResult search(String text) throws IOException {
        ElasticsearchClient client = EsClientUtil.getEsClient();

        // 获取用户收藏歌曲列表
        Long userId = ThreadLocalUtil.getUserId();
        Set<Long> ids = userFavoriteMapper.getUserFavoriteSongIds(userId);

        SearchResponse<Song> response = client.search(s -> s
                        .index("music")
                        .query(q -> q
                                .multiMatch(m -> m
                                        .fields("songName", "style", "artistName", "album", "lyrics")
                                        .query(text)
                                        .type(TextQueryType.Phrase)
                                )
                        )
                        .highlight(h -> h
                                .fields("songName", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                                .fields("artistName", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                                .fields("album", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                                .fields("lyrics", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                        .numberOfFragments(1)
                                        .fragmentSize(20)
                                )
                                .fields("style", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                        )
                        .size(10),  // 返回前10条
                Song.class
        );

        List<Song> result=new ArrayList<>();
        long total=0L;

        // 高亮处理
        if (response != null) {
            total = response.hits().total().value();
            log.info("共查询到{}条记录", total);

            List<Hit<Song>> hits = response.hits().hits();
            for (Hit<Song> hit : hits) {
                Map<String, List<String>> highlight = hit.highlight();


                Song source = hit.source();


                // 覆盖非高亮结果
                if (highlight.containsKey("artistName")) {
                    source.setArtistName(highlight.get("artistName").get(0));
                }
                if (highlight.containsKey("album")) {
                    source.setAlbum(highlight.get("album").get(0));
                }
                if (highlight.containsKey("lyrics")) {
                    source.setLyricsSegment(highlight.get("lyrics").get(0));
                }
                if (highlight.containsKey("songName")) {
                    source.setSongName(highlight.get("songName").get(0));
                }
                if (highlight.containsKey("style")) {
                    source.setStyle(highlight.get("style").get(0));
                }

                if(ids.contains(source.getSongId())) {
                    source.setIsFavorite(true);
                }
                // 获取歌曲播放次数
                Object o = stringRedisTemplate.opsForHash().get("favorite:song:", source.getSongId().toString());
                source.setFavoriteCount(o==null?0L:(Long) o);


                log.info("处理后的文本：{}", source.toString());
                result.add(source);

            }
        }
        return new PageResult(total,result);
    }

    @Override
    public void add(SongDTO songDTO) {
        Long userId = ThreadLocalUtil.getUserId();
        User user = userMapper.selectByIdWithArtistName(userId);
        Long artistId = user.getArtistId();

        Artist artist = artistMapper.selectById(artistId);

        if (artistId == null) {
            // TODO 业务中可能会导致产生僵尸文件，需要后续优化
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        Song song = new Song();
        BeanUtil.copyProperties(songDTO, song);
        song.setArtistId(artistId);
        song.setArtistName(artist.getArtistName());
        save(song);
    }



    // 提前准备好一批候选歌曲（RECOMMEND_POOL_SIZE大小，可能100~200首）
    // 缓存30分钟，避免每次推荐请求都查数据库
    // 后续的pickRecommendSongs方法从这个池子里随机取，不再查库
    private List<Song> loadRecommendPool(Long userId, List<Long> sortedStyleIds, List<Long> favoriteSongIds) {
        String redisKey = RECOMMEND_REDIS_KEY_PREFIX + userId;
        List<String> cachedJsonList = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        if (cachedJsonList != null && !cachedJsonList.isEmpty()) {
            return cachedJsonList.stream()
                    .map(json -> JSONUtil.toBean(json, Song.class))
                    .collect(Collectors.toList());
        }

        List<Song> candidateSongs = baseMapper.getRecommendedSongsByStyles(
                sortedStyleIds, favoriteSongIds, RECOMMEND_POOL_SIZE);
        if (candidateSongs.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> jsonList = candidateSongs.stream()
                .map(JSONUtil::toJsonStr)
                .toList();
        stringRedisTemplate.opsForList().rightPushAll(redisKey, jsonList);
        stringRedisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
        return candidateSongs;
    }



    private void handleCoverAndAudioDelete(Song song) {
        // 删除歌曲、歌曲封面
        String coverUrl = song.getCoverUrl();
        String audioUrl = song.getAudioUrl();

        if (coverUrl != null && !coverUrl.isEmpty()) {
            minIOService.deleteFile(song.getCoverUrl());
        }

        if (audioUrl != null && !audioUrl.isEmpty()) {
            minIOService.deleteFile(song.getAudioUrl());
        }
    }


    private void handleStyleUpdate(String style, Long songId) {

        if (style == null || style.isEmpty() || songId == null) {
            return;
        }

        List<String> styleNameList = List.of(style.split(","));
        List<Style> styles = styleNameList.stream().map(s -> new Style(null, s)).toList();

        // 查询已存在的音乐风格
        QueryWrapper<Style> queryWrapper = new QueryWrapper<Style>()
                .in("name", styleNameList);
        List<Style> existStyleList = styleMapper.selectList(queryWrapper);
        Set<String> existStyleNameSet = existStyleList.stream().map(Style::getName).collect(Collectors.toSet());

        // 获取所有风格id
        List<Long> styleIdList = new ArrayList<>(existStyleList.stream().map(Style::getStyleId).toList());

        // 获取所有不存在的风格
        List<Style> notExistStyleList = styles.stream()
                .filter(s -> !existStyleNameSet.contains(s.getName()))
                .toList();

        // 若风格不存在则新增音乐风格（添加空集合判断）
        if (!notExistStyleList.isEmpty()) {
            styleMapper.insertBatch(notExistStyleList);
            for (Style notExistStyle : notExistStyleList) {
                styleIdList.add(notExistStyle.getStyleId());
            }
        }

        // 新增歌曲-风格（也需要判断）
        if (!styleIdList.isEmpty()) {
            List<Genre> genreList = styleIdList.stream()
                    .map(styleId -> new Genre(songId, styleId))
                    .toList();
            genreMapper.insertBatch(genreList);
        }
    }

}




