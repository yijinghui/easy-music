package com.easy.pojo.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PlaylistInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String coverUrl;

    private Long playCount;

    private Long songCount;
}
