package com.easy.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.*;
import com.easy.pojo.entity.Playlist;
import com.easy.pojo.vo.PlaylistVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PlaylistService extends IService<Playlist> {

    PageResult list(PlaylistPageQueryDTO pageQueryDTO);

    void create(PlaylistDTO playlistDTO);

    void addSongs(Long playlistId, List<Long> songIds);

    void deleteSongs(Long playlistId, List<Long> songIds);

    void delete(Long playlistId);

    void addSong(Long playlistId, Long songId);

    void removeSong(Long playlistId, Long songId);

    List<Playlist> listByUserId(Long userId);

    void update(@Valid PlaylistDTO playlistDTO);

    PageResult search(@NotBlank String text, PageQueryDTO pageQueryDTO) throws IOException;

    PlaylistVO getDetailById(Long playlistId);

    void updateCover(Long playlistId, MultipartFile cover);

}
