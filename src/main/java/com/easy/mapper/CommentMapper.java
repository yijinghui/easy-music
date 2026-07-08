package com.easy.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.pojo.entity.Comment;
import com.easy.pojo.vo.CommentInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;


@Mapper
public interface CommentMapper extends BaseMapper<Comment> {



    /**
     * 滚动分页查询评论
     * @param songId
     * @param playlistId
     * @param rootId
     * @param firstId
     * @return
     */
    List<CommentInfoVO> scrollPageComment(Long songId, Long playlistId, Long rootId, Long firstId);


    /**
     * 获取热门评论
     * @param songId
     * @param playlistId
     * @return
     */
    List<CommentInfoVO> selectHotComment(Long songId, Long playlistId);






}
