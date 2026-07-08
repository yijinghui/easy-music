
package com.easy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.pojo.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 音乐歌房Mapper
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {



    @Update("UPDATE tb_music_room SET room_status=#{status} WHERE id=#{roomId}")
    void updateRoomStatus(Long roomId, int status);

    /**
     * 根据房间id列表查询房间信息，包含用户信息
     * @param ids 房间id列表
     * @return 房间信息列表
     */


    List<Room> listWithUserInfo(List<Long> ids);
}
