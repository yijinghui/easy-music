
package com.easy.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 音乐歌房实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_music_room")
public class Room implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌房ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long roomId;

    /**
     * 歌房名称
     */
    @TableField("room_name")
    private String roomName;



    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 创建者昵称
     */
    @TableField(exist = false)
    private String creatorNickname;

    /**
     * 创建者头像
     */
    @TableField(exist = false)
    private String creatorAvatar;



    /**
     * 歌房状态：0-关闭，1-正常
     */
    @TableField("room_status")
    private Integer roomStatus;

    /**
     * 最大人数
     */
    @TableField("max_users")
    private Integer maxUsers;

    /**
     * 在线人数
     */
    @Schema(description = "当前在线人数", example = "5", minimum = "0")
    @TableField(exist = false)
    private Integer onlineCount;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;


    /**
     * 关闭时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("close_time")
    private LocalDateTime closeTime;
}
