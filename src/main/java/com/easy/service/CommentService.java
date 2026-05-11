package com.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.CommentPlaylistDTO;
import com.easy.pojo.dto.CommentScrollQueryDTO;
import com.easy.pojo.dto.CommentSongDTO;
import com.easy.pojo.entity.Comment;
import com.easy.pojo.vo.CommentVO;
import com.easy.result.Result;
import com.easy.result.ScrollResult;

public interface CommentService extends IService<Comment> {
    Result addSongComment(CommentSongDTO commentSongDTO);

    Result addPlaylistComment(CommentPlaylistDTO commentPlaylistDTO);

    Result likeComment(Long commentId,Integer likeStatus);

    Result deleteComment(Long commentId);

    Result<ScrollResult<CommentVO>> listSongComment(CommentScrollQueryDTO queryDTO);
}
