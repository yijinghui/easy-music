package com.easy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.pojo.entity.Song;
import com.easy.pojo.vo.SongAdminVO;
import io.lettuce.core.dynamic.annotation.Key;
import lombok.Generated;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface SongMapper extends BaseMapper<Song> {


    IPage<SongAdminVO> getAllSongs(@Param("page") Page<SongAdminVO> page,
                                   @Param("artistId") Long artistId,
                                   @Param("songName") String songName,
                                   @Param("album") String album);

}
