package com.easy.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_playlist_song")
public class PlaylistSong implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 歌单 id
     */
    @TableField("playlist_id")
    private Long playlistId;

    /**
     * 歌曲 id
     */
    @TableField("song_id")
    private Long songId;
}
