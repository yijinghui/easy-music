package com.easy.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PlaylistPageQueryDTO extends PageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单 id
     */
    @Schema(description = "歌单 id", example = "1")
    private Long playlistId;

    /**
     * 歌单标题
     */
    @Schema(description = "歌单标题", example = "国风纯音乐精选集")
    private String title;

    /**
     * 歌单风格
     */
    @Schema(description = "歌单风格", example = "国风")
    private String style;

    /**
     * 用户 id
     */
    @Schema(description = "用户id", example = "27")
    private Long userId;



}
