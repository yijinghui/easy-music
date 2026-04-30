package com.easy.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "歌手查询请求参数")
public class ArtistPageQueryDTO implements Serializable {
    // Serializable 是一个标记接口，用于告诉 Java 虚拟机，这个类的对象可以被序列化（转换成字节流）和反序列化（从字节流恢复成对象）。

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码", example = "1", required = true)
    private Integer pageNum;

    /**
     * 每页数量
     */
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    @Schema(description = "每页数量", example = "10", required = true)
    private Integer pageSize;

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

}
