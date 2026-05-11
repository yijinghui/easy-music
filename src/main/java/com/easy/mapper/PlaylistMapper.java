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

    Page<PlaylistVO> selectPageWithPlaylistInfo(Page<Playlist> page, @Param("dto")PlaylistPageQueryDTO pageQueryDTO);


    List<PlaylistInfoVO> selectPlaylistInfo(Long userId);
}
