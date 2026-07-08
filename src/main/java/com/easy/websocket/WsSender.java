package com.easy.websocket;

import com.alibaba.fastjson.JSONObject;
import com.easy.utils.RoomSessionManager;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WsSender {

    private final RoomSessionManager roomSessionManager;

    public void sendError(Session session, String error) {
        JSONObject message = new JSONObject();
        message.put("action", "error");
        message.put("message", error);
        sendMessage(session, message);
    }

    public void sendErrorAndClose(Session session, String error) {
        sendError(session, error);
        try {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY, error
            ));
        } catch (IOException e) {
            log.error("关闭连接失败", e);
        }
    }

    public void sendMessage(Session session, JSONObject message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message.toJSONString());
            }
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    public void broadcastRoom(Long roomId, JSONObject message) {
        Map<String, Session> sessions = roomSessionManager.getRoomSessions(roomId);
        if (sessions == null) {
            return;
        }

        String msgStr = message.toJSONString();
        for (Session session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(msgStr);
                }
            } catch (IOException e) {
                log.error("广播消息失败", e);
            }
        }
    }
}
