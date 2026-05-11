package com.easy.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class CommentVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    private Long commentId;

    /**
     * 发表评论的用户名
     */
    private String username;

    /**
     * 发表评论用户头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 点赞数量
     */
    private Long likeCount;

    /**
     * 回复的用户的用户名
     */
    private String replyUsername;


    /**
     * 是否点赞
     */
    private Boolean isLike;
}
