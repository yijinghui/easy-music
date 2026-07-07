package com.easy.pojo.dto;

import com.easy.pojo.dto.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@Schema(description = "评论数据传输对象")
public class CommentDTO implements Serializable {

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


    @NotNull(message = "父级评论ID不能为空")
    @Schema(description = "父级评论ID（0表示一级评论）", example = "0")
    private Long parentId;

    @NotNull(message = "父级评论ID不能为空")
    @Schema(description = "根评论ID（0或null表示一级评论）", example = "1001")
    private Long rootId;
}
