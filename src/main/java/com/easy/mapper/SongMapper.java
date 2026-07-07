package com.easy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.pojo.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface SongMapper extends BaseMapper<Song> {


    IPage<Song> getAllSongs(@Param("page") Page<Song> page,
                              @Param("artistId") Long artistId,
                              @Param("style") String style,
                              @Param("songName") String songName,
                              @Param("id") Long id,
                              @Param("album") String album);




    IPage<Song> getUserFavoriteSongs(@Param("page") Page<Song> page,
                                       @Param("userId") Long userId);

    // 获取随机歌曲列表
    @Select("""
                SELECT id AS songId,
                    name AS songName,
                    artist_id AS artistId,
                    album AS album,
                    duration AS duration,
                    style AS style,
                    cover_url AS coverUrl,
                    audio_url AS audioUrl,
                    release_time AS releaseTime,
                    artist_name AS artistName,
                    favorite_count AS favoriteCount,
                    lyrics AS lyrics,
                    lyrics_head AS lyricsHead,
                    nested AS nested
                FROM tb_song
                WHERE id >= (
                    SELECT FLOOR(RAND() * (SELECT MAX(id) FROM tb_song))
                )
                LIMIT 14;
            """)
    List<Song> getRandomSongs();

    List<Long> getFavoriteSongStyles(@Param("songIds") List<Long> songIds);

    List<Song> getRecommendedSongsByStyles(@Param("styleIds") List<Long> styleIds,
                                              @Param("excludeSongIds") List<Long> excludeSongIds,
                                              @Param("limit") int limit);

    IPage<Song> getSongsByPlaylistId(Page<Song> page, Long playlistId);




    List<Song> getByIds(@Param("songIds") List<Long> songIds);
}
