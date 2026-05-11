package com.easy.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.pojo.entity.Comment;
import com.easy.pojo.vo.CommentInfoVO;
import com.easy.pojo.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    CommentInfoVO selectCommentInfo(Long commentId);

    List<CommentInfoVO> selectCommentInfos(List<Long> commentIds);
}
