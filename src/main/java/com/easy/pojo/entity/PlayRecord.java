package com.easy.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_play_record")
public class PlayRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("song_id")
    private Long songId;

    @TableField("playlist_id")
    private Long playlistId;

    @TableField("create_time")
    private LocalDateTime createTime;


    @TableField("is_deleted")
    private Integer isDeleted;
}
