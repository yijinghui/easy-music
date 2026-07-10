package com.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.dto.SongDTO;
import com.easy.pojo.dto.SongPageQueryDTO;
import com.easy.pojo.entity.Song;
import com.easy.result.PageResult;
import com.easy.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SongService extends IService<Song> {
    PageResult list(SongPageQueryDTO pageQueryDTO);

    Result addSong(SongDTO songDTO);

    Result updateSong(SongDTO songDTO);

    void updateCover(Long songId, MultipartFile cover);

    void updateAudio(Long songId, MultipartFile audio, String duration);

    Result deleteSong(Long songId);

    Result deleteSongs(List<Long> songIds);

    List<Song> getRecommendedSongs();

    List<Song> getTop200SongsByMonth(Integer offset);

    List<Song> getTop200SongsByWeek(Integer offset);

    void listen(Long songId, Long playListId);

    PageResult search(String text) throws IOException;

    void add(@Valid SongDTO songDTO);

    PageResult listSongByPlaylistId(@NotBlank Long playlistId, PageQueryDTO pageQueryDTO);
}
