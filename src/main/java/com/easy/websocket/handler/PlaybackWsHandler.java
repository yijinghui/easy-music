package com.easy.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.easy.mapper.RoomMapper;
import com.easy.mapper.SongMapper;
import com.easy.pojo.model.UserSessionInfo;
import com.easy.pojo.entity.Room;
import com.easy.pojo.entity.Song;
import com.easy.utils.RoomSessionManager;
import com.easy.websocket.WsMessageFactory;
import com.easy.websocket.WsSender;
import com.easy.websocket.model.RoomPlayState;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class PlaybackWsHandler {

    private final RoomSessionManager roomSessionManager;
    private final RoomMapper roomMapper;
    private final SongMapper songMapper;
    private final WsMessageFactory messageFactory;
    private final RoomStateHandler roomStateHandler;
    private final WsSender wsSender;


    public void handlePlay(Session session, JSONObject json) {
        UserSessionInfo info = roomSessionManager.getSessionInfo(session.getId());
        Long roomId = json.getLong("roomId"), userId = info.getUserId(), songId = json.getLong("songId"), progress = json.getLong("progress");
        if (!hasPermission(roomId, userId)) {
            wsSender.sendError(session, "没有权限");
            return;
        }
        String songPayload = getSongPayload(songId);
        wsSender.broadcastRoom(roomId, messageFactory.play(roomId, songId, progress, songPayload));
        roomStateHandler.play(roomId, songId, progress, songPayload);
        log.info("歌房 {} 开始播放歌曲 {}", roomId, songId);
    }

    public void handlePause(Session session, JSONObject json) {
        UserSessionInfo info = roomSessionManager.getSessionInfo(session.getId());
        Long roomId = json.getLong("roomId");
        Long songId = json.getLong("songId");
        Long userId = info.getUserId();
        Long progress = json.getLong("progress");
        if (!hasPermission(roomId, userId)) {
            wsSender.sendError(session, "没有权限");
            return;
        }
        roomStateHandler.pause(roomId, progress);
        wsSender.broadcastRoom(roomId, messageFactory.pause(roomId, songId, progress));
        log.info("歌房 {} 暂停播放歌曲 {}", roomId, songId);
    }

    public void handleSeek(Session session, JSONObject json) {
        UserSessionInfo info = roomSessionManager.getSessionInfo(session.getId());
        Long roomId = json.getLong("roomId");
        Long songId = json.getLong("songId");
        Long userId = info.getUserId();
        Long progress = json.getLong("progress");
        if (!hasPermission(roomId, userId)) {
            wsSender.sendError(session, "没有权限");
            return;
        }
        roomStateHandler.seek(roomId, progress);
        wsSender.broadcastRoom(roomId, messageFactory.seek(roomId, songId, progress));
        log.info("歌房 {} 跳转播放歌曲 {} 到进度 {}", roomId, songId, progress);
    }

    public void handleStop(Session session, JSONObject json) {
        UserSessionInfo info = roomSessionManager.getSessionInfo(session.getId());
        Long roomId = json.getLong("roomId"), userId = info.getUserId();
        if (!hasPermission(roomId, userId)) {
            wsSender.sendError(session, "没有权限");
            return;
        }
        roomStateHandler.stop(roomId);
        wsSender.broadcastRoom(roomId, messageFactory.stop(roomId));
        log.info("歌房 {} 停止播放", roomId);
    }

    public void handleSync(Session session, JSONObject json) {
        Long roomId = json.getLong("roomId");

        // 获取当前播放的歌曲信息
        RoomPlayState playState = roomStateHandler.getPlayState(roomId);
        Long songId = playState.getSongId();

        String songPayload = getSongPayload(songId);
        wsSender.sendMessage(session, messageFactory.sync(
                roomId,
                playState,
                songPayload,
                roomSessionManager.getRoomSessionCount(roomId)));
    }



    public boolean hasPermission(Long roomId, Long userId) {
        Room room = roomMapper.selectById(roomId);
        return room != null && room.getCreatorId().equals(userId);
    }


    public String getSongPayload(Long songId) {
        Song song = songId != null ? songMapper.selectById(songId) : null;
        return JSON.toJSONString(song);
    }
}
