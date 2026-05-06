package com.easy.pojo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PlaylistSongPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", example = "10", defaultValue = "10")
    private Integer pageSize;

    /**
     * 歌单 id
     */
    @Schema(description = "歌单 id", example = "1")
    private Long playlistId;


}
