package com.easy.pojo.dto;

import com.easy.pojo.dto.group.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 歌房数据传输对象
 *
 * @author YourName
 * @date 2026-07-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "歌房信息DTO")
public class RoomDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌房ID
     */
    @Schema(description = "歌房ID", example = "1001")
    @NotNull(groups = UpdateGroup.class, message = "歌房ID不能为空")
    private Long roomId;

    /**
     * 歌房名称
     */
    @Schema(description = "歌房名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "听歌")
    @NotBlank(message = "歌房名称不能为空")
    @Size(max = 50, message = "歌房名称长度不能超过50个字符")
    private String roomName;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "创建者ID不能为空")
    private Long creatorId;


    /**
     * 歌房状态：0-关闭，1-正常
     */
    @Schema(description = "歌房状态：0-关闭，1-正常", allowableValues = {"0", "1"}, example = "1")
    @NotNull(message = "歌房状态不能为空")
    @Min(value = 0, message = "歌房状态值范围0-1")
    @Max(value = 1, message = "歌房状态值范围0-1")
    private Integer roomStatus;

    /**
     * 最大人数
     */
    @Schema(description = "最大人数", example = "20")
    @NotNull(message = "最大人数不能为空")
    @Min(value = 1, message = "最大人数至少为1")
    @Max(value = 100, message = "最大人数不能超过100")
    private Integer maxUsers;
}
