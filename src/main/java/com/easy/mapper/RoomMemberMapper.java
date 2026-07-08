
package com.easy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.pojo.entity.RoomMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 歌房成员Mapper
 */
@Mapper
public interface RoomMemberMapper extends BaseMapper<RoomMember> {

    /**
     * 根据歌房ID查询成员列表
     */
    List<RoomMember> selectByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据歌房ID和用户ID查询成员
     */
    RoomMember selectByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 更新成员在线状态
     */
    @Update("UPDATE tb_room_member SET online_status = #{status} WHERE room_id = #{roomId} AND user_id = #{userId}")
    int updateOnlineStatus(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 标记成员离开
     */
    @Update("UPDATE tb_room_member SET leave_time = NOW(), online_status = 0 WHERE room_id = #{roomId} AND user_id = #{userId}")
    int markLeave(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 查询歌房在线人数
     */
    Integer countOnlineMembers(@Param("roomId") Long roomId);
}
