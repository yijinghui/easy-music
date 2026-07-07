package com.easy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<Map<String, Object>> getDailyNewUsers(LocalDate begin, LocalDate end);

    @Select("SELECT " +
            "u.id AS userId, " +
            "u.artist_id AS artistId, " +
            "u.username AS username, " +
            "u.phone AS phone, " +
            "u.email AS email, " +
            "u.user_avatar AS userAvatar, " +
            "u.introduction AS introduction, " +
            "u.create_time AS createTime, " +
            "u.update_time AS updateTime, " +
            "a.name AS artistName " +
            "FROM tb_user u " +
            "LEFT JOIN tb_artist a ON u.artist_id = a.id " +
            "WHERE u.id = #{userId}")
    User selectByIdWithArtistName(Long userId);
}
