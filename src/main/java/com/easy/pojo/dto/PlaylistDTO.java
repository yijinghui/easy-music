package com.easy.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.easy.pojo.dto.group.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PlaylistDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 歌单id
     */
    @Schema(description = "歌单id", example = "1")
    @NotNull(groups = {UpdateGroup.class}, message = "歌单id不能为空")
    private Long playlistId;

    /**
     * 歌单标题
     */
    @Schema(description = "歌单标题", example = "EasyMusic官方歌单")
    @NotBlank(message = "歌单标题不能为空")
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
    @NotBlank(message = "歌单风格不能为空")
    private String style;







}
