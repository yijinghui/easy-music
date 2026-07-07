package com.easy.utils;

import com.easy.pojo.model.UserSessionInfo;
import jakarta.websocket.Session;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomSessionManager {

    private static final Map<Long, Map<String, Session>> ROOM_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, UserSessionInfo> SESSION_INFO = new ConcurrentHashMap<>();

    public void addSessionInfo(String sessionId, UserSessionInfo userSessionInfo) {
        SESSION_INFO.put(sessionId, userSessionInfo);
    }

    public UserSessionInfo getSessionInfo(String sessionId) {
        return SESSION_INFO.get(sessionId);
    }

    public UserSessionInfo removeSessionInfo(String sessionId) {
        return SESSION_INFO.remove(sessionId);
    }

    public void updateSessionRoom(String sessionId,Long roomId) {
        UserSessionInfo userSessionInfo = SESSION_INFO.get(sessionId);
        if (userSessionInfo == null) return;
        userSessionInfo.setRoomId(roomId);
    }

    public Map<String, Session> getRoomSessions(Long roomId) {
        return ROOM_SESSIONS.get(roomId);
    }

    public Session getRoomSession(String sessionId, Long roomId) {
        Map<String, Session> sessions = ROOM_SESSIONS.get(roomId);
        if (sessions == null) return null;
        return sessions.get(sessionId);
    }

    public Map<String, Session> removeRoomSessions(Long roomId) {
        return ROOM_SESSIONS.remove(roomId);
    }
    public void addRoomSession(Session session, Long roomId) {
        ROOM_SESSIONS.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
    }


    public int getRoomSessionCount(Long roomId) {
        if (ROOM_SESSIONS.get(roomId) == null) return 0;
        return ROOM_SESSIONS.get(roomId).size();
    }

    public void removeRoomSession(String sessionId, Long roomId) {
        Map<String, Session> sessions = ROOM_SESSIONS.get(roomId);
        if (sessions == null) return;
        sessions.remove(sessionId);
    }


}
