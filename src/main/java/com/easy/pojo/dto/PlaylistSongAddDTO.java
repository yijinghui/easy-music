package com.easy.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Data
public class PlaylistSongAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单 id
     */
    @Schema(description = "歌单 id", example = "1")
    private Long playlistId;

    /**
     * 歌曲 id列表
     */
    @Schema(description = "歌曲 id列表", example = "[2,3,4]")
    private List<Long> songIds;
}
