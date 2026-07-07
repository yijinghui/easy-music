package com.easy.pojo.dto;

import com.easy.pojo.dto.group.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class SongDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "歌曲id", example = "1")
    @NotNull(groups = {UpdateGroup.class},message = "歌曲ID不能为空")
    private Long songId;

    /**
     * 歌手 id
     */
    @Schema(description = "歌手id", example = "1")
    private Long artistId;

    /**
     * 歌名
     */
    @Schema(description = "歌曲名称", example = "晴天")
    @NotBlank(message = "歌曲名称不能为空")
    private String songName;

    /**
     * 专辑
     */
    @Schema(description = "专辑名称", example = "叶惠美")
    private String album;

    /**
     * 歌词
     */
    @Schema(description = "歌词", example = "故事的小黄花,从出生那年就飘着," +
            "童年的荡秋千,随记忆一直晃到现在,Re So So Si Do Si La,So La Si Si Si Si La Si La So," +
            "吹着前奏望着天空,我想起花瓣试着掉落,为你翘课的那一天,花落的那一天,教室的那一间,我怎么看不见,消失的下雨天," +
            "我好想再淋一遍,没想到失去的勇气我还留着,好想再问一遍,你会等待还是离开,刮风这天我试过握着你手," +
            "但偏偏雨渐渐大到我看你不见,还要多久我才能在你身边,等到放晴的那天也许我会比较好一点," +
            "从前从前有个人爱你很久,但偏偏风渐渐把距离吹得好远,好不容易又能再多爱一天," +
            "但故事的最后你好像还是说了拜拜,为你翘课的那一天,花落的那一天,教室的那一间,我怎么看不见," +
            "消失的下雨天,我好想再淋一遍,没想到失去的勇气我还留着,好想再问一遍,你会等待还是离开," +
            "刮风这天我试过握着你手,但偏偏雨渐渐大到我看你不见,还要多久我才能在你身边," +
            "等到放晴的那天也许我会比较好一点,从前从前有个人爱你很久,偏偏风渐渐把距离吹得好远," +
            "好不容易又能再多爱一天,但故事的最后你好像还是说了拜拜,刮风这天我试过握着你手,但偏偏雨渐渐大到我看你不见," +
            "还要多久我才能够在你身边,等到放晴那天也许我会比较好一点,从前从前有个人爱你很久," +
            "但偏偏雨渐渐把距离吹得好远,好不容易又能再多爱一天,但故事的最后你好像还是说了拜")
    private String lyrics;

    /**
     * 歌曲风格
     */
    @Schema(description = "歌曲风格", example = "流行音乐")
    private String style;

    /**
     * 歌曲发行时间
     */
    @Schema(description = "歌曲发行时间", example = "2003-07-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;

    /**
     * 歌曲时长
     */
    @Schema(description = "歌曲时长", example = "120")
    @NotBlank(message = "歌曲时长不能为空")
    private String duration;

}
