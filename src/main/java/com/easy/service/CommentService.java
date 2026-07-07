package com.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.CommentScrollQueryDTO;
import com.easy.pojo.dto.CommentDTO;
import com.easy.pojo.entity.Comment;
import com.easy.pojo.vo.CommentInfoVO;
import com.easy.result.Result;
import com.easy.result.ScrollResult;

public interface CommentService extends IService<Comment> {
    void add(Long songId, Long playlistId, CommentDTO commentDTO);

    void likeComment(Long commentId,Integer likeStatus);

    void delete(Long commentId);

    ScrollResult list(Long songId,
                                     Long playlistId,
                                     CommentScrollQueryDTO queryDTO);

    void refreshHotComment();
}
