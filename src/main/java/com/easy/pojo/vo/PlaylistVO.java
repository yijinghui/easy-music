package com.easy.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

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
     * 所属用户名
     */
    private String username;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 播放次数
     */
    private Long playCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;



    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


}
