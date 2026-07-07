package com.easy.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;


@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "歌手认证查询参数")
public class ArtistAuthPageQueryDTO extends PageQueryDTO{

    @Schema(description = "歌手认证id", example = "1")
    private Long id;
    /**
     * 歌手id
     */
    @Schema(description = "歌手id", example = "1")
    private Long artistId;

    /**
     * 认证状态
     */
    @Schema(description = "认证状态",example = "0")
    private Integer status;
    /**
     * 用户id
     */
    @Schema(description = "用户id", example = "1")
    private String userId;
    /**
     * 申请时间
     */
    @Schema(description = "申请起始时间", example = "2023-05-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createStartTime;

    @Schema(description = "申请结束时间", example = "2023-05-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createEndTime;

    /**
     * 审核时间
     */
    @Schema(description = "审核起始时间", example = "2023-05-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate auditStartTime;

    @Schema(description = "审核结束时间", example = "2023-05-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate auditEndTime;
}
