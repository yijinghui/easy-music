package com.easy.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;


    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String introduction;


    /**
     * 歌手ID
     */
    private Long artistId;

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
