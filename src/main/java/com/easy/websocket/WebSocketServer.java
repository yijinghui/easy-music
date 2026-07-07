package com.easy.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.easy.mapper.UserMapper;
import com.easy.pojo.model.UserSessionInfo;
import com.easy.pojo.entity.User;
import com.easy.utils.JwtUtil;
import com.easy.utils.RoomSessionManager;
import com.easy.utils.SpringBeanUtil;

import com.easy.websocket.handler.ChatWsHandler;
import com.easy.websocket.handler.PlaybackWsHandler;
import com.easy.websocket.handler.RoomWsHandler;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ServerEndpoint("/ws/room")
@Slf4j
public class WebSocketServer {

    private final UserMapper userMapper;
    private final ChatWsHandler chatWsHandler;
    private final RoomWsHandler roomWsHandler;
    private final PlaybackWsHandler playbackWsHandler;
    private final RoomSessionManager roomSessionManager;
    private final WsSender wsSender;


    public WebSocketServer() {
        userMapper = SpringBeanUtil.getBean(UserMapper.class);
        chatWsHandler = SpringBeanUtil.getBean(ChatWsHandler.class);
        roomWsHandler = SpringBeanUtil.getBean(RoomWsHandler.class);
        playbackWsHandler = SpringBeanUtil.getBean(PlaybackWsHandler.class);
        roomSessionManager = SpringBeanUtil.getBean(RoomSessionManager.class);
        wsSender = SpringBeanUtil.getBean(WsSender.class);
    }

    @OnOpen
    public void onOpen(Session session) {
        Map<String, Object> claims = parseClaims(session);
        if (claims == null || claims.isEmpty()) {
            wsSender.sendErrorAndClose(session, "未登录");
            return;
        }
        Long userId = Long.valueOf(claims.get("userId").toString());
        User user = userMapper.selectById(userId);
        roomSessionManager.addSessionInfo(session.getId(), userInfo(userId, user));
        log.info("用户{}创建连接", userId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug("收到消息: {}", message);
        JSONObject json = JSON.parseObject(message);
        switch (json.getString("action")) {
            case "join" -> roomWsHandler.handleJoinRoom(session, json);
            case "leave" -> roomWsHandler.handleLeaveRoom(session, json);
            case "heartbeat" -> roomWsHandler.handleHeartbeat(session, json);
            case "sync" -> playbackWsHandler.handleSync(session, json);
            case "play" -> playbackWsHandler.handlePlay(session, json);
            case "pause" -> playbackWsHandler.handlePause(session, json);
            case "seek" -> playbackWsHandler.handleSeek(session, json);
            case "stop" -> playbackWsHandler.handleStop(session, json);
            case "chat" -> chatWsHandler.handleChat(session, json);
        }
    }

    @OnClose
    public void onClose(Session session) {
        UserSessionInfo info = roomSessionManager.removeSessionInfo(session.getId());
        if (info != null && info.getRoomId() != null) {
            roomSessionManager.removeRoomSession(session.getId(), info.getRoomId());
            log.info("用户 {} 断开连接", info.getUserId());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误", error);
    }

    private Map<String, Object> parseClaims(Session session) {
        String query = session.getQueryString();
        String token = null;
        if (query == null) return null;
        for (String param : query.split("&")){
            if (param.startsWith("token=")){
                token = param.substring(6);
            }
        }
        if (token == null || token.isEmpty()) return null;
        try {
            return JwtUtil.parseToken(token);
        } catch (Exception e) {
            log.warn("WebSocket连接认证失败: {}", e.getMessage());
            wsSender.sendErrorAndClose(session, "认证失败");
            return null;
        }
    }

    private UserSessionInfo userInfo(Long userId, User user) {
        UserSessionInfo info = new UserSessionInfo();
        info.setUserId(userId);
        info.setRoomId(null);
        info.setNickname(user.getUsername());
        info.setAvatar(user.getUserAvatar());
        return info;
    }

}
