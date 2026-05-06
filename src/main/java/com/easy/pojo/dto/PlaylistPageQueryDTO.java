package com.easy.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PlaylistPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @NotNull
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", example = "10", defaultValue = "10")
    @NotNull
    private Integer pageSize;

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
    @Schema(description = "用户 id", example = "27")
    private Long userId;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "create_time")
    private String orderBy;

    /**
     * 排序方式
     */
    @Schema(description = "排序方式", example = "desc")
    private String sortRule;


}
