
package com.easy.service;

import com.easy.pojo.dto.RoomDTO;
import com.easy.pojo.entity.Room;
import com.easy.pojo.model.ChatMessage;
import com.easy.result.Result;

import java.util.List;

/**
 * 音乐歌房服务接口
 */
public interface RoomService {

    /**
     * 获取活跃歌房列表
     */
    List<Room> listActiveRooms();

    Room create(RoomDTO roomDTO);

    List<ChatMessage> listChatMessages(Long roomId);
}
