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
    Result<Long> getAllPlaylistsCount(String style);

    Result<PageResult> getAllPlaylists(PlaylistPageQueryDTO pageQueryDTO);

    void create(PlaylistDTO playlistDTO);

    Result updatePlaylist(PlaylistDTO playlistDTO);

    Result updatePlaylistCover(Long playlistId, MultipartFile cover);

    Result deletePlaylist(Long playlistId);

    Result deletePlaylists(List<Long> playlistIds);

    Result addPlaylistSongs(Long playlistId, List<Long> songIds);

    List<Playlist>  getPlaylistInfo(Long userId);

    Result<PageResult> getSongsByPlaylistId(Long playlistId, PageQueryDTO pageQueryDTO);

    Result deletePlaylistSongs(Long playlistId,List<Long> songIds);

    void delete(Long playlistId);

    Result updatePlaylistByUser(PlaylistDTO playlistDTO, Long userId);

    void addSong(Long playlistId, Long songId);

    void removeSong(Long playlistId, Long songId);

    List<Playlist> listByUserId(Long userId);

    void update(@Valid PlaylistDTO playlistDTO);

    PageResult search(@NotBlank String text) throws IOException;


    PlaylistVO getDetailById(Long playlistId);
}
