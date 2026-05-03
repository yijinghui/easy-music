package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.mapper.GenreMapper;
import com.easy.mapper.SongMapper;
import com.easy.mapper.StyleMapper;
import com.easy.pojo.dto.SongAddDTO;
import com.easy.pojo.dto.SongPageQueryDTO;
import com.easy.pojo.dto.SongUpdateDTO;
import com.easy.pojo.entity.Genre;
import com.easy.pojo.entity.Song;
import com.easy.pojo.entity.Style;
import com.easy.pojo.vo.SongAdminVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.MinIOService;
import com.easy.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService{

    private final SongMapper songMapper;

    private final StyleMapper styleMapper;

    private final GenreMapper genreMapper;

    private final StringRedisTemplate redisTemplate;

    private final MinIOService minIOService;

    @Override
    public Result<Long> getAllSongsCount(String style) {

        LambdaQueryWrapper<Song> queryWrapper = new LambdaQueryWrapper<Song>()
                .like(Song::getStyle, style);

        return Result.success(songMapper.selectCount(queryWrapper));
    }

    @Override
    public Result<PageResult<SongAdminVO>> getAllSongs(SongPageQueryDTO pageQueryDTO) {

        Page<SongAdminVO> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());

        Long artistId = pageQueryDTO.getArtistId();
        String songName = pageQueryDTO.getSongName();
        String album = pageQueryDTO.getAlbum();
        IPage<SongAdminVO> songPage = songMapper.getAllSongs(page,artistId, songName, album);

        if (songPage.getRecords().isEmpty()) {
            return Result.success("未找到相关数据", new PageResult<>(0L, null));
        }

        return Result.success(new PageResult<>(songPage.getTotal(), songPage.getRecords()));
    }



    @Transactional(rollbackFor = Exception.class)
    public Result addSong(SongAddDTO songAddDTO) {

        Song song = new Song();
        BeanUtil.copyProperties(songAddDTO, song);
        songMapper.insert(song);

        Long songId = song.getSongId();
        String style = song.getStyle();

        // 获取歌曲所有风格
        handleStyleUpdate(style, songId);

        return Result.success("添加成功");

    }


    @Transactional(rollbackFor = Exception.class)
    public Result updateSong(SongUpdateDTO songUpdateDTO) {
        Song song = new Song();
        BeanUtil.copyProperties(songUpdateDTO, song);

        // 查询歌曲是否存在
        Song songDB = songMapper.selectById(song.getSongId());
        if (songDB == null){
            return Result.error("歌曲不存在");
        }

        String oldStyle = songDB.getStyle();
        String newStyle = song.getStyle();
        Long songId = song.getSongId();

        BeanUtil.copyProperties(song, songDB);

        songMapper.updateById(songDB);

        if (newStyle==null){
            // 删除歌曲-风格
            genreMapper.delete(new QueryWrapper<Genre>().eq("song_id", songId));
            return Result.success("更新成功");
        }

        if (oldStyle.equals(newStyle)){
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
        Song songDB = songMapper.selectById(songId);
        if (songDB == null){
            return Result.error("歌曲不存在");
        }
        songDB.setCoverUrl(coverUrl);
        songMapper.updateById(songDB);

        return Result.success("更新成功");

    }

    @Override
    public Result updateSongAudio(Long songId, MultipartFile audio, String duration) {
        String audioUrl = minIOService.uploadFile(audio, "songAudios");

        // 查询目标歌曲是否存在
        Song songDB = songMapper.selectById(songId);
        if (songDB == null){
            return Result.error("歌曲不存在");
        }
        songDB.setAudioUrl(audioUrl);
        songDB.setDuration(duration);
        songMapper.updateById(songDB);

        return Result.success("更新成功");

    }

    @Transactional(rollbackFor = Exception.class)
    public Result deleteSong(Long songId) {
        Song songDB = songMapper.selectById(songId);
        if (songDB == null){
            return Result.error("歌曲不存在");
        }
        songMapper.deleteById(songId);

        genreMapper.delete(new QueryWrapper<Genre>().eq("song_id", songId));

        // 删除歌曲、歌曲封面
        handleCoverAndAudioDelete(songDB);


        return Result.success("删除成功");
    }

    @Override
    public Result deleteSongs(List<Long> songIds) {

        // 批量查询歌曲
        List<Song> songList = songMapper.selectByIds(songIds);
        if (songList.isEmpty()){
            return Result.error("歌曲不存在");
        }

        Set<Long> songIdSet = songList.stream().map(Song::getSongId).collect(Collectors.toSet());
        List<Long> notExistSongIds = songIds.stream().filter(songId -> !songIdSet.contains(songId)).toList();

        songMapper.delete(new QueryWrapper<Song>().in("song_id", songIdSet));

        for (Song song : songList){
            handleCoverAndAudioDelete(song);
        }

        if (!notExistSongIds.isEmpty()){
            return Result.success("删除成功,歌曲"+notExistSongIds+"不存在");
        }

        return Result.success("删除成功");


    }

    private void handleCoverAndAudioDelete(Song song) {
        // 删除歌曲、歌曲封面
        String coverUrl = song.getCoverUrl();
        String audioUrl = song.getAudioUrl();

        if (coverUrl!= null&&!coverUrl.isEmpty()){
            minIOService.deleteFile(song.getCoverUrl());
        }

        if (audioUrl!= null&&!audioUrl.isEmpty()){
            minIOService.deleteFile(song.getAudioUrl());
        }
    }


    private void handleStyleUpdate(String style, Long songId) {

        if(style == null || style.isEmpty()|| songId == null){
            return;
        }

        List<String> styleNameList = List.of(style.split(","));
        List<Style> styles = styleNameList.stream().map(s -> new Style(null,s)).toList();


        // 查询已存在的音乐风格
        QueryWrapper<Style> queryWrapper = new QueryWrapper<Style>()
                .in("name", styleNameList);
        List<Style> exsitStyleList = styleMapper.selectList(queryWrapper);
        Set<String> exsitStyleNameSet = exsitStyleList.stream().map(Style::getName).collect(Collectors.toSet());

        // 获取所有风格id
        List<Long> styleIdList = new ArrayList<>(exsitStyleList.stream().map(Style::getStyleId).toList());

        // 获取所有不存在的风格
        List<Style> notExistStyleList = styles.stream().filter(s -> !exsitStyleNameSet.contains(s.getName())).toList();

        // 若风格不存在则新增音乐风格
        styleMapper.insertBatch(notExistStyleList);
        for (Style notExistStyle : notExistStyleList){
            styleIdList.add(notExistStyle.getStyleId());
        }

        // 新增歌曲-风格
        List<Genre> genreList = styleIdList.stream().map(styleId -> new Genre(songId, styleId)).toList();
        genreMapper.insertBatch(genreList);
    }





}
