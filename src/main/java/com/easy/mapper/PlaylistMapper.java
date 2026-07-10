package com.easy.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.pojo.dto.PlaylistPageQueryDTO;
import com.easy.pojo.entity.Playlist;
import com.easy.pojo.vo.PlaylistInfoVO;
import com.easy.pojo.vo.PlaylistVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {

    Page<PlaylistVO> selectPageWithUsername(Page<Playlist> page, @Param("dto") PlaylistPageQueryDTO pageQueryDTO);


    List<PlaylistInfoVO> selectPlaylistInfo(Long userId);

    Page<PlaylistVO> getUserFavoritePlaylists(@Param("page") Page<PlaylistVO> page,
                                              @Param("userId") Long userId);

    @Select(" select " +
            " p.id as playlistId," +
            " p.title as title," +
            " p.cover_url as coverUrl," +
            " p.introduction as introduction," +
            " p.style as style," +
            " p.user_id as userId," +
            " p.create_time as createTime," +
            " p.update_time as updateTime," +
            " p.play_count as playCount," +
            " u.username as username" +
            " from tb_playlist p" +
            " left join tb_user u on p.user_id = u.id" +
            " where p.id = #{playlistId}")
    PlaylistVO selectDetailById(Long playlistId);
}
