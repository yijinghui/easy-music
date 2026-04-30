package com.easy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.pojo.entity.Artist;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ArtistMapper extends BaseMapper<Artist> {
}
