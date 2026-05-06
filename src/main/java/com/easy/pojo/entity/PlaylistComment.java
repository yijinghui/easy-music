package com.easy.pojo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_playlist_comment")
public class PlaylistComment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 歌单 id
     */
    @TableField(value = "playlist_id")
    private Long playlistId;

    /**
     * 用户 id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Long likeCount;

    /**
     * 父评论 id
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 评论状态
     */
    @TableField("status")
    private Integer status;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;


    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
