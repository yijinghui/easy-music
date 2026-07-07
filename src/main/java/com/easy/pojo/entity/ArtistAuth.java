package com.easy.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 歌手认证申请表实体类
 */
@Data
@TableName("tb_artist_auth")
public class ArtistAuth {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 歌手ID
     */
    private Long artistId;

    /**
     * 认证资料（营业执照URL等）
     */
    private String businessLicenseUrl;

    /**
     * 审核状态：0-待审核，1-已通过，2-已拒绝，3-已取消
     */
    private Integer status;

    /**
     * 审核拒绝原因
     */
    private String reason;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 审核管理员ID
     */
    private Long adminId;

    /**
     * 申请时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
