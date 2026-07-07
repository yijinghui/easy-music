package com.easy.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SongPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @NotNull
    private Integer pageNum;

    /**
     * 每页数量
     */
    @NotNull
    private Integer pageSize;

    /**
     * id
     */
    private Long id;

    /**
     * 歌手
     */
    private Long artistId;

    /**
     * 歌曲类型
     */
    private String style;


    /**
     * 歌曲名
     */
    private String songName;

    /**
     * 专辑
     */
    private String album;

    /**
     * 风格
     */

}
