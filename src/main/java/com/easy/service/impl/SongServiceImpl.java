package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import com.easy.utils.MSUtil;
import com.easy.utils.ThreadLocalUtil;
import com.meilisearch.sdk.Client;
import com.minio.MinioTemplate;
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

    private final UserMapper userMapper;

    private final UserFavoriteMapper userFavoriteMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final MinioTemplate minIOService;

    private final PlayRecordMapper playRecordMapper;
    private final PlaylistMapper playlistMapper;
    private final ArtistMapper artistMapper;

    private final MSUtil msUtil;


    @Override
    public PageResult list(SongPageQueryDTO pageQueryDTO) {

        Page<Song> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        String style = pageQueryDTO.getStyle();
        Long artistId = pageQueryDTO.getArtistId();
        String songName = pageQueryDTO.getSongName();
        String album = pageQueryDTO.getAlbum();
        Long songId = pageQueryDTO.getId();

        Page<Song> result = lambdaQuery().eq(StrUtil.isNotBlank(style), Song::getStyle, style)
                .eq(artistId != null, Song::getArtistId, artistId)
                .like(StrUtil.isNotBlank(songName), Song::getSongName, songName)
                .like(StrUtil.isNotBlank(album), Song::getAlbum, album)
                .eq(songId != null, Song::getSongId, songId)
                .page(page);
        return new PageResult(result.getTotal(), result.getRecords());

    }


    @Transactional(rollbackFor = Exception.class)
    public Result addSong(SongDTO songDTO) {

        Song song = new Song();
        BeanUtil.copyProperties(songDTO, song, "songId");
        baseMapper.insert(song);

        Long songId = song.getSongId();
        String style = song.getStyle();


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

        String newStyle = song.getStyle();
        Long songId = song.getSongId();

        BeanUtil.copyProperties(song, songDB);

        baseMapper.updateById(songDB);
        return Result.success("更新成功");

    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCover(Long songId, MultipartFile cover) {
        String coverUrl = minIOService.uploadFile(cover, "songCovers");
        // 查询目标歌曲是否存在
        Song songDB = baseMapper.selectById(songId);
        if (songDB == null) {
            throw new IllegalArgumentException("歌曲不存在");
        }
        songDB.setCoverUrl(coverUrl);
        baseMapper.updateById(songDB);
    }

    @Override
    public void updateAudio(Long songId, MultipartFile audio, String duration) {
        String audioUrl = minIOService.uploadFile(audio, "songAudios");

        // 查询目标歌曲是否存在
        Song song = new Song();
        song.setSongId(songId);
        song.setAudioUrl(audioUrl);
        song.setDuration(duration);
        save(song);

    }

    @Transactional(rollbackFor = Exception.class)
    public Result deleteSong(Long songId) {


        Song songDB = baseMapper.selectById(songId);



        if (baseMapper.deleteById(songId) == 0) {
            return Result.error("歌曲不存在");
        }

        return Result.success("已删除歌曲" + songDB.getSongName());
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
    public PageResult listSongByArtistId(Long artistId, PageQueryDTO pageQueryDTO) {
        Long userId = ThreadLocalUtil.getUserId();
        Page<Song> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        Page<Song> result = lambdaQuery()
                .eq(Song::getArtistId, artistId)
                .orderByDesc(Song::getPlayCount)
                .page(page);


        List<Song> songs = result.getRecords();
        // 设置用户是否收藏
        Set<Long> ids = userFavoriteMapper.getUserFavoriteSongIds(userId);
        songs.forEach(song -> {
            if(ids.contains(song.getSongId())) {
                song.setIsFavorite(true);
            }
        });
        return new PageResult(result.getTotal(), songs);
    }

    @Override
    public PageResult search(String text) throws IOException {

        List<Object> result = msUtil.search("music", text, Arrays.asList("songName", "artistName", "album", "style", "lyricsSegment"));

        // 转化为Song对象
        List<Song> songs = result.stream()
                .map(obj -> {
                    // 先将对象转为标准 JSON
                    String jsonStr = JSONUtil.toJsonStr(obj);
                    return JSONUtil.toBean(jsonStr, Song.class);
                })
                .toList();

        // 查询歌曲的完整数据
        if (songs.isEmpty()) {
            return new PageResult(0L, new ArrayList<>());
        }

        List<Song> fullSongs = baseMapper.getByIds(songs.stream().map(Song::getSongId).toList());

        // 用户是否收藏
        Long userId = ThreadLocalUtil.getUserId();
        Set<Long> ids = userFavoriteMapper.getUserFavoriteSongIds(userId);

        // 合并完整数据
        for (int i = 0; i < fullSongs.size(); i++) {
            fullSongs.get(i).setSongName(songs.get(i).getSongName());
            fullSongs.get(i).setArtistName(songs.get(i).getArtistName());
            fullSongs.get(i).setAlbum(songs.get(i).getAlbum());
            fullSongs.get(i).setStyle(songs.get(i).getStyle());
            fullSongs.get(i).setLyricsSegment(songs.get(i).getLyricsSegment());
            if (ids.contains(fullSongs.get(i).getSongId())) {
                fullSongs.get(i).setIsFavorite(true);
            }
            log.info("song: {}", fullSongs.get(i));
        };

        return new PageResult((long) fullSongs.size(), fullSongs);

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


}




