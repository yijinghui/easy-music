
package com.easy.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.exception.BaseException;
import com.easy.mapper.RoomMapper;
import com.easy.pojo.dto.RoomDTO;
import com.easy.pojo.entity.Room;
import com.easy.pojo.model.ChatMessage;
import com.easy.service.RoomService;
import com.easy.utils.RoomSessionManager;
import com.easy.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 音乐歌房服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {


    private final StringRedisTemplate stringRedisTemplate;

    private final RoomSessionManager roomSessionManager;

    @Override
    public List<Room> listActiveRooms() {
        // 从redis中随机获取的在线房间id
        Set<String> ids = stringRedisTemplate.opsForSet().members("room:online");
        if(ids==null|| ids.isEmpty()){
            return List.of();
        }
        List<Long> randomIds = ids.stream().map(Long::parseLong).toList();
        // 根据数据库查询房间

        List<Room> rooms=baseMapper.listWithUserInfo(randomIds);
        for(Room room:rooms){
            room.setOnlineCount(roomSessionManager.getRoomSessionCount(room.getRoomId()));
        }
        return rooms;
    }


    @Override
    public Room create(RoomDTO roomDTO) {
        Long userId = ThreadLocalUtil.getUserId();

        Room mr = new Room();
        mr.setCreatorId(userId);
        mr.setRoomName(roomDTO.getRoomName());
        mr.setRoomStatus(1);
        mr.setMaxUsers(roomDTO.getMaxUsers());
        mr.setCreateTime(LocalDateTime.now());

        save(mr);
        stringRedisTemplate.opsForSet().add("room:online",mr.getRoomId().toString());
        return mr;
    }

    @Override
    public List<ChatMessage> listChatMessages(Long roomId) {
        String key = "room:chat:" + roomId;
        List<String> messageStr = stringRedisTemplate.opsForList().range(key,-20,-1);
        if(messageStr==null|| messageStr.isEmpty()){
            return List.of();
        }
        return messageStr.stream()
                .map(str -> JSONUtil.parseObj(str).toBean(ChatMessage.class))
                .collect(Collectors.toList());
    }


}
