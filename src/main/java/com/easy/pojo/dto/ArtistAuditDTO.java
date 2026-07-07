package com.easy.pojo.dto;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ArtistAuditDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "认证记录id", example = "1")
    private Long id;

    @Schema(description = "歌手id", example = "1")
    private Long artistId;

    @Schema(description = "用户id", example = "1")
    private Long userId;

    @Schema(description = "审核管理员id", example = "1")
    private Long adminId;


    @Schema(description = "认证状态", example = "1")
    private Integer status;

    @Schema(description = "审核理由", example = "无")
    private String reason;



}
