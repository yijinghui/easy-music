package com.easy.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "添加歌手请求参数")
public class ArtistAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手姓名
     */
    @NotBlank(message = "歌手姓名不能为空")
    @Size(min = 1, max = 50, message = "歌手姓名长度必须在1-50字符之间")
    @Schema(description = "歌手姓名", example = "周杰伦", required = true)
    private String artistName;

    /**
     * 歌手性别：0-男，1-女
     */
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别参数错误：0-男，1-女")
    @Max(value = 1, message = "性别参数错误：0-男，1-女")
    @Schema(description = "歌手性别（0-男，1-女）", example = "0", required = true, allowableValues = {"0", "1"})
    private Integer gender;

    /**
     * 歌手出生日期
     */
    @Past(message = "出生日期必须是过去的日期")
    @NotNull(message = "出生日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "歌手出生日期", example = "1979-01-18", required = true)
    private LocalDate birth;

    /**
     * 歌手所处地区
     */
    @NotBlank(message = "地区不能为空")
    @Size(max = 100, message = "地区长度不能超过100字符")
    @Schema(description = "歌手所处地区", example = "中国台湾", required = true)
    private String area;

    /**
     * 歌手简介
     */
    @Size(max = 2000, message = "简介长度不能超过2000字符")
    @Schema(description = "歌手简介", example = "华语流行乐男歌手、音乐人、演员、导演、编剧。")
    private String introduction;

}
