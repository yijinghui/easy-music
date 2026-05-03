package com.easy.service;

import com.easy.pojo.dto.SongAddDTO;
import com.easy.pojo.dto.SongPageQueryDTO;
import com.easy.pojo.dto.SongUpdateDTO;
import com.easy.pojo.vo.SongAdminVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SongService {
    Result<Long> getAllSongsCount(String style);

    Result<PageResult<SongAdminVO>> getAllSongs(SongPageQueryDTO pageQueryDTO);

    Result addSong(SongAddDTO songAddDTO);

    Result updateSong(SongUpdateDTO songUpdateDTO);

    Result updateSongCover(Long songId, MultipartFile cover);

    Result updateSongAudio(Long songId, MultipartFile audio, String duration);

    Result deleteSong(Long songId);

    Result deleteSongs(List<Long> songIds);
}
