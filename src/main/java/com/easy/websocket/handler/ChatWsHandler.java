package com.easy.websocket.handler;

import com.alibaba.fastjson.JSONObject;
import com.easy.pojo.model.ChatMessage;
import com.easy.pojo.model.UserSessionInfo;
import com.easy.service.RoomService;
import com.easy.utils.RoomSessionManager;
import com.easy.utils.SensitiveWordUtil;
import com.easy.websocket.WsMessageFactory;
import com.easy.websocket.WsSender;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWsHandler {

    private final WsSender wsSender;
    private final WsMessageFactory messageFactory;
    private final RoomSessionManager roomSessionManager;
    private final SensitiveWordUtil  sensitiveWordUtil;
    private final RoomStateHandler roomStateHandler;

    public void handleChat(Session session, JSONObject json) {
        String content = json.getString("content");
        Long roomId = json.getLong("roomId");
        UserSessionInfo userInfo = roomSessionManager.getSessionInfo(session.getId());

        // 过滤敏感词
        content = sensitiveWordUtil.filter(content);

        // 将消息信息存入redis中
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserId(userInfo.getUserId());
        chatMessage.setNickname(userInfo.getNickname());
        chatMessage.setAvatar(userInfo.getAvatar());
        chatMessage.setContent(content);
        chatMessage.setCreateTime(LocalDateTime.now().toString());
        roomStateHandler.addChatMessage(roomId, chatMessage);

        // 广播聊天消息
        wsSender.broadcastRoom(roomId, messageFactory.chat(roomId, userInfo,content));
        log.info("用户{}发消息：{}", userInfo.getUserId(), json.getString("content"));
    }
}
