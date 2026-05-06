package com.easy.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PlaylistUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单 id
     */
    @Schema(description = "歌单 id", example = "1")
    private Long playlistId;

    /**
     * 歌单标题tb_playlist
     */
    @Schema(description = "歌单标题", example = "title01")
    private String title;

    /**
     * 歌单简介
     */
    @Schema(description = "歌单简介", example = "introduction01")
    private String introduction;

    /**
     * 歌单风格
     */
    @Schema(description = "歌单风格", example = "style01")
    private String style;

    /**
     * 所属用户id
     */
    @Schema(description = "所属用户id", example = "1")
    private Long userId;



}
