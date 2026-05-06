package com.easy.pojo.vo;

import lombok.Data;

import java.io.Serial;

@Data
public class PlaylistSongVO {


        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 歌曲 id
         */
        private Long playlistId;

        /**
         * 歌手
         */
        private String artistName;

        /**
         * 歌曲 id
         */
        private Long songId;

        /**
         * 歌名
         */
        private String songName;

        /**
         * 专辑
         */
        private String album;


        /**
         * 歌曲时长
         */
        private String duration;

        /**
         * 歌曲风格
         */
        private String style;

        /**
         * 歌曲封面 url
         */
        private String coverUrl;

        /**
         * 歌曲 url
         */
        private String audioUrl;



}
