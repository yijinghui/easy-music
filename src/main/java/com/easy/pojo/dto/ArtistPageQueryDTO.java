package com.easy.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "歌手查询请求参数")
public class ArtistPageQueryDTO extends PageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "歌手id", example = "1")
    private  Long artistId;

    /**
     * 歌手姓名
     */
    @Schema(description = "歌手姓名（支持模糊查询）", example = "周杰伦")
    private String artistName;

    /**
     * 歌手性别：0-男，1-女
     */
    @Min(value = 0, message = "性别参数错误：0-男，1-女")
    @Max(value = 1, message = "性别参数错误：0-男，1-女")
    @Schema(description = "歌手性别（0-男，1-女）", example = "0", allowableValues = {"0", "1"})
    private Integer gender;

    /**
     * 歌手所处地区
     */
    @Schema(description = "歌手所处地区（支持模糊查询）", example = "中国台湾")
    private String area;

    /**
     * 认证状态
     */
    @Min(value = 0, message = "认证状态参数错误：0-未认证 1-待审核，2-已认证")
    @Max(value = 2, message = "认证状态参数错误：0-未认证 1-待审核，2-已认证")
    @Schema(description = "0-未认证 1-待审核，2-已认证",example = "0")
    private Integer status;

}
