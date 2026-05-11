package com.easy.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CommentSongDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌曲id
     */
    private Long songId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父级评论id
     */
    private Long parentId;

    /**
     * 跟评论id
     */
    private Long rootId;

}
