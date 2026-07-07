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
     * 用户的关注数
     */
    private Long followCount;

    /**
     * 用户粉丝数
     */
    private Long fansCount;

}
