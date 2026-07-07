package com.easy.websocket.handler;

import com.alibaba.fastjson.JSONObject;
import com.easy.mapper.RoomMapper;
import com.easy.pojo.model.UserSessionInfo;
import com.easy.pojo.entity.Room;
import com.easy.utils.RoomSessionManager;
import com.easy.websocket.WsMessageFactory;
import com.easy.websocket.WsSender;
import com.easy.websocket.model.RoomPlayState;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomWsHandler {
    private final RoomSessionManager roomSessionManager;
    private final WsSender  wsSender;
    private final WsMessageFactory messageFactory;
    private final PlaybackWsHandler playbackWsHandler;
    private final RoomStateHandler roomStateHandler;
    private final RoomMapper roomMapper;

    public void handleJoinRoom(Session session, JSONObject json) {
        UserSessionInfo userInfo = roomSessionManager.getSessionInfo(session.getId());
        Long roomId = json.getLong("roomId");
        Long userId = userInfo.getUserId();
        if (userInfo.getRoomId() != null) {
            wsSender.sendError(session, "您已在其他房间中，请先离开");
            return;
        }
        Map<String, Session> roomSessions = roomSessionManager.getRoomSessions(roomId);
        if (roomSessions != null && roomSessions.containsKey(session.getId())) {
            wsSender.sendError(session, "您已在房间中");
            return;
        }

        // 添加用户至房间会话管理器
        roomSessionManager.addRoomSession(session, roomId);
        roomSessionManager.updateSessionRoom(session.getId(), roomId);
        // 广播加入房间消息
        wsSender.broadcastRoom(
                roomId, messageFactory.userJoin(roomId, userInfo, roomSessionManager.getRoomSessionCount(roomId)));
        // 处理同步消息
        playbackWsHandler.handleSync(session, json);
        log.info("用户 {} 加入歌房 {}", userId, roomId);
    }

    public void handleLeaveRoom(Session session, JSONObject json) {
        UserSessionInfo userInfo = roomSessionManager.getSessionInfo(session.getId());
        Long roomId = json.getLong("roomId"), userId = userInfo.getUserId();

        // 判断用户是否是房间创建者
        if (hasPermission(roomId, userId)){
            closeRoom(roomId);
        } else{
            leaveRoom(session, roomId);
        }
        log.info("用户 {} 离开歌房 {}", userId, roomId);
    }



    public void handleHeartbeat(Session session, JSONObject json) {
        Long roomId = json.getLong("roomId");
        Long clientProgress = json.getLong("progress");

        RoomPlayState state = roomStateHandler.getPlayState(roomId);
        long serverProgress = calculateServerProgress(state);
        if (Math.abs(serverProgress - clientProgress) > 300) {
            // 进度差超过300ms，发送同步消息
            playbackWsHandler.handleSync(session, json);
            return;
        }
        // 进度差在300ms内，发送正常心跳消息
        wsSender.sendMessage(session, messageFactory.heartbeatNormal(roomId, serverProgress));
    }


    private void closeRoom(Long roomId) {
        Map<String, Session> sessions = roomSessionManager.getRoomSessions(roomId);
        if (sessions != null) {
            for (Session s : sessions.values()) {
                roomSessionManager.updateSessionRoom(s.getId(), null);
                wsSender.sendMessage(s, messageFactory.roomClose());
            }
        }
        // 删除播放状态
        roomStateHandler.stop(roomId);
        // 删除房间在线状态
        roomStateHandler.removeOnlineRoom(roomId);
        // 删除房间会话
        roomSessionManager.removeRoomSessions(roomId);
        // 更新房间状态
        roomMapper.updateRoomStatus(roomId, 0);
    }

    private void leaveRoom(Session session, Long roomId) {
        UserSessionInfo info = removeSessionFromRoom(roomId, session.getId());
        if (info != null){
            wsSender.broadcastRoom(
                    roomId,
                    messageFactory.userLeave(roomId, info, roomSessionManager.getRoomSessionCount(roomId)));
        }
    }


    private UserSessionInfo removeSessionFromRoom(Long roomId, String sessionId) {
        roomSessionManager.removeRoomSession(sessionId, roomId);
        Map<String, Session> sessions = roomSessionManager.getRoomSessions(roomId);
        if (sessions != null && sessions.isEmpty()) roomSessionManager.removeRoomSessions(roomId);
        UserSessionInfo info = roomSessionManager.getSessionInfo(sessionId);
        roomSessionManager.updateSessionRoom(sessionId, null);
        return info;
    }



    public long calculateServerProgress(RoomPlayState playState) {
        long progress =playState.getProgress() == null ? 0L : playState.getProgress();
        if (playState.getPlayStatus() == 1) { // 歌曲正在播放
            return progress + System.currentTimeMillis() - playState.getStartTime();
        }
        return progress;
    }

    public boolean hasPermission(Long roomId, Long userId) {
        Room room = roomMapper.selectById(roomId);
        return room != null && room.getCreatorId().equals(userId);
    }
}
