package com.easy.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CommentScrollQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 根评论ID
     */
    @NotNull(message = "根评论ID不能为空")
    @Schema(description = "根评论ID",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1")
    private Long rootId;

    /**
     * 滚动分页查询的起始ID
     */
    @NotNull(message = "查询的起始ID不能为空")
    @Schema(description = "滚动分页查询的起始ID（第一次查询传0或最大值，后续传上一页最后一条记录的ID）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1")
    private Long firstId;

}
