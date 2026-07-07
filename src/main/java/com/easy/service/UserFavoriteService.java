package com.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.entity.UserFavorite;
import com.easy.result.PageResult;
import com.easy.result.Result;


public interface UserFavoriteService extends IService<UserFavorite> {

    PageResult getUserFavoriteSongs(PageQueryDTO pageQueryDTO);

    void collectSong(Long songId);

    void cancelCollectSong(Long songId);

    PageResult getUserFavoritePlaylists(PageQueryDTO pageQueryDTO);

    void collectPlaylist(Long playlistId);

    void cancelCollectPlaylist(Long playlistId);
}
