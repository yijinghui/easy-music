package com.easy.service;


import com.easy.pojo.dto.*;
import com.easy.pojo.entity.Playlist;
import com.easy.pojo.vo.PlaylistSongVO;
import com.easy.pojo.vo.PlaylistVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PlaylistService{
    Result<Long> getAllPlaylistsCount(String style);

    Result<PageResult<PlaylistVO>> getAllPlaylists(PlaylistPageQueryDTO pageQueryDTO);

    Result addPlaylist(PlaylistAddDTO playlistAddDTO);

    Result updatePlaylist(PlaylistUpdateDTO playlistUpdateDTO);

    Result updatePlaylistCover(Long playlistId, MultipartFile cover);

    Result deletePlaylist(Long playlistId);

    Result deletePlaylists(List<Long> playlistIds);

    Result addPlaylistSongs(PlaylistSongAddDTO addDTO);

    Result<PageResult<PlaylistSongVO>> getPlaylistSongs(PlaylistSongPageQueryDTO pageQueryDTO);
}
