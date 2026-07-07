package com.easy.pojo.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommentInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long commentId;

    /**
     * 根级评论id
     */
    private Long rootId;

    /**
     * 父级评论id
     */
    private Long parentId;

    /**
     * 发表评论的用户id
     */
    private Long userId;

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
     * 回复的用户的用户名
     */
    private String replyUsername;

    /**
     * 子评论个数
     */
    private Long childrenCount;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 是否点过赞
     */
    private Boolean isLiked;

    /**
     * 是否是热评
     */
    private Integer isHot;

}
