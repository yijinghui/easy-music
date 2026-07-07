package com.easy.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.easy.pojo.model.ChatMessage;
import com.easy.websocket.model.RoomPlayState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RoomStateHandler {

    private static final String PLAY_KEY_PREFIX = "room:play:";
    private static final String ONLINE_KEY = "room:online";
    private static final String FIELD_SONG_ID = "songId";
    private static final String FIELD_START_TIME = "startTime";
    private static final String FIELD_PROGRESS = "progress";
    private static final String FIELD_PLAY_STATUS = "playStatus";
    private static final String FIELD_SONG = "song";
    private static final int PLAYING = 1;
    private static final String CHAT_MESSAGE_PREFIX = "room:chat:";

    private final StringRedisTemplate stringRedisTemplate;


    public void play(Long roomId, Long songId, Long progress, String songPayload) {
        String playKey = PLAY_KEY_PREFIX + roomId;
        stringRedisTemplate.opsForHash().put(playKey, FIELD_SONG_ID, songId.toString());
        stringRedisTemplate.opsForHash().put(playKey, FIELD_START_TIME, String.valueOf(System.currentTimeMillis()));
        stringRedisTemplate.opsForHash().put(playKey, FIELD_SONG, songPayload);
        stringRedisTemplate.opsForHash().put(playKey, FIELD_PROGRESS, progress.toString());
        stringRedisTemplate.opsForHash().put(playKey, FIELD_PLAY_STATUS, String.valueOf(PLAYING));
    }

    public void pause(Long roomId, Long progress) {
        String playKey = PLAY_KEY_PREFIX + roomId;
        stringRedisTemplate.opsForHash().put(playKey, FIELD_PROGRESS, progress.toString());
        stringRedisTemplate.opsForHash().put(playKey, FIELD_PLAY_STATUS, "0");
    }

    public void seek(Long roomId, Long progress) {
        String playKey = PLAY_KEY_PREFIX + roomId;
        stringRedisTemplate.opsForHash().put(playKey, FIELD_PROGRESS, progress.toString());
        stringRedisTemplate.opsForHash().put(playKey, FIELD_START_TIME, String.valueOf(System.currentTimeMillis()));
    }

    public void stop(Long roomId) {
        stringRedisTemplate.delete(PLAY_KEY_PREFIX + roomId);
    }


    public RoomPlayState getPlayState(Long roomId) {
        String playKey = PLAY_KEY_PREFIX + roomId;
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(playKey);

        if (map == null || map.isEmpty()) {
            return new RoomPlayState(0L, System.currentTimeMillis(), 0L, 0, "");
        }

        Object songIdObj = map.get(FIELD_SONG_ID);
        Object startTimeObj = map.get(FIELD_START_TIME);
        Object progressObj = map.get(FIELD_PROGRESS);
        Object playStatusObj = map.get(FIELD_PLAY_STATUS);
        Object songObj = map.get(FIELD_SONG);

        Long songId = songIdObj != null ? Long.parseLong(songIdObj.toString()) : 0L;
        Long startTime = startTimeObj != null ? Long.parseLong(startTimeObj.toString()) : System.currentTimeMillis();
        Long progress = progressObj != null ? Long.parseLong(progressObj.toString()) : 0L;
        Integer playStatus = playStatusObj != null ? Integer.parseInt(playStatusObj.toString()) : 0;
        String song = songObj != null ? songObj.toString() : "";

        return new RoomPlayState(songId, startTime, progress, playStatus, song);
    }

    public void removeOnlineRoom(Long roomId) {
        stringRedisTemplate.opsForSet().remove(ONLINE_KEY, roomId.toString());
    }


    public void addChatMessage(Long roomId, ChatMessage chatMessage) {
        String chatKey = CHAT_MESSAGE_PREFIX + roomId;
        stringRedisTemplate.opsForList().rightPush(chatKey, JSON.toJSONString(chatMessage));
    }


}
