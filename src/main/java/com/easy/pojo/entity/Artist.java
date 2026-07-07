package com.easy.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_artist")
public class Artist implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long artistId;

    /**
     * 歌手姓名
     */
    @TableField("name")
    private String artistName;

    /**
     * 歌手性别：0-男，1-女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 歌手头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 歌手出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("birth")
    private LocalDate birth;

    /**
     * 歌手所处地区
     */
    @TableField("area")
    private String area;



    /**
     * 歌手简介
     */
    @TableField("introduction")
    private String introduction;

    /**
     * 歌手关联的用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 歌手状态
     */
    @TableField("status")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
