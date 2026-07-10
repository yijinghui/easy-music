package com.easy.pojo.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserStatVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户收藏的歌曲数
     */
    private Integer favoriteSongCount;

    /**
     * 用户收藏的歌单数
     */
    private Integer favoritePlaylistCount;


    /**
     * 用户创建的歌单数
     */
    private Integer createdPlaylistCount;

}
