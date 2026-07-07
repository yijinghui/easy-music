package com.easy.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单id
     */
    private Long playlistId;

    /**
     * 歌单标题
     */
    private String title;


    /**
     * 歌单类型
     */
    private String style;

    /**
     * 歌单封面
     */
    private String coverUrl;

    /**
     * 歌单简介
     */
    private String introduction;

    /**
     * 创建用户ID
     */
    private Long userId;


    /**
     * 所属用户姓名
     */
    private String username;



    /**
     * 收藏数
     */
    private Long likeCount;

    /**
     * 播放次数
     */
    private Long playCount;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;

    /**
     * 是否收藏
     */
    @TableField(exist = false)
    private Boolean isFavorite;




}
