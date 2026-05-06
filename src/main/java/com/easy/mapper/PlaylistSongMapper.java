package com.easy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.pojo.entity.PlaylistSong;
import com.easy.pojo.vo.PlaylistSongVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface PlaylistSongMapper extends BaseMapper<PlaylistSong> {

    void insertBatch(List<PlaylistSong> songList);

    Page<PlaylistSongVO> selectPageWithSongInfo(Page<PlaylistSongVO> page, Long playlistId);
}
