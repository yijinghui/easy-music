package com.easy.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PlaylistAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单标题
     */
    @Schema(description = "歌单标题", example = "EasyMusic官方歌单")
    private String title;

    /**
     * 歌单简介
     */
    @Schema(description = "歌单简介", example = "动感十足的欧美流行歌，让有氧运动更有活力~")
    private String introduction;

    /**
     * 歌单风格
     */
    @Schema(description = "歌单风格", example = "欧美流行")
    private String style;

    /**
     * 所属用户id
     */
    @Schema(description = "所属用户id", example = "1")
    private Long userId;

}
