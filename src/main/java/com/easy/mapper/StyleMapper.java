package com.easy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.pojo.entity.Style;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StyleMapper extends BaseMapper<Style> {

    int insertBatch(@Param("list") List<Style> styleList);
}
