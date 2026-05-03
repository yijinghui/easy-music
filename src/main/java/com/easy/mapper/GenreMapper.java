package com.easy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.pojo.entity.Genre;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GenreMapper extends BaseMapper<Genre> {

    int insertBatch(@Param("list") List<Genre> genreList);
}
