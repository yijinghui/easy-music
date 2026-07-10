package com.easy.pojo.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.easy.pojo.dto.group.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Data
public class CommentPageQueryDTO extends PageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;



    @Schema(description = "评论ID", example = "1001")
    @NotNull(groups = UpdateGroup.class, message = "评论ID不能为空")
    private Long commentId;


    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "12345", hidden = true)
    private Long userId;


    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500个字符之间")
    @Schema(description = "评论内容", example = "这首歌太好听了！")
    private String content;


    /**
     * 歌曲 id
     */
    @NotNull(message = "歌曲ID不能为空")
    @Schema(description = "歌曲ID", example = "12345", hidden = true)
    private Long songId;

    /**
     * 歌单 id
     */
    @NotNull(message = "歌单ID不能为空")
    @Schema(description = "歌单ID", example = "12345", hidden = true)
    private Long playlistId;




    @NotNull(message = "是否热门不能为空")
    @Schema(description = "是否热门（0-否，1-是）", example = "0")
    private Integer isHot;



}
