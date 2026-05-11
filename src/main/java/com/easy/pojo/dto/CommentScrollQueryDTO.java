package com.easy.pojo.dto;


import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CommentScrollQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌曲/歌单ID
     */
    private Long id;

    /**
     * 根评论ID
     */
    private Long rootId;

    /**
     * 最大时间戳
     */
    private Long maxTime;

    /**
     * 偏移量
     */
    private Integer offset;
}
