package com.easy.websocket;

import com.alibaba.fastjson.JSONObject;
import com.easy.pojo.model.UserSessionInfo;
import com.easy.websocket.model.RoomPlayState;

import org.springframework.stereotype.Component;

@Component
public class WsMessageFactory {


    public JSONObject userJoin(Long roomId,UserSessionInfo userInfo, int onlineCount) {
        JSONObject message = baseMessage("userJoin", roomId);
        message.put("userId", userInfo.getUserId());
        message.put("nickname", userInfo.getNickname());
        message.put("avatar", userInfo.getAvatar());
        message.put("onlineCount", onlineCount);
        return message;
    }

    public JSONObject userLeave(Long roomId, UserSessionInfo userInfo, int onlineCount) {
        JSONObject message = baseMessage("userLeave", roomId);
        message.put("userId", userInfo.getUserId());
        message.put("userNickname", userInfo.getNickname());
        message.put("onlineCount", onlineCount);
        return message;
    }

    public JSONObject roomClose() {
        JSONObject message = new JSONObject();
        message.put("action", "roomClose");
        return message;
    }

    public JSONObject sync(Long roomId, RoomPlayState playState, String songPayload, int onlineCount) {
        JSONObject message = baseMessage("sync", roomId);
        message.put("song", songPayload);
        message.put("startTime", playState.getStartTime());
        message.put("progress", playState.getProgress());
        message.put("playStatus", playState.getPlayStatus());
        message.put("onlineCount", onlineCount);
        return message;
    }

    public JSONObject heartbeatNormal(Long roomId, long serverProgress) {
        JSONObject message = baseMessage("heartbeat", roomId);
        message.put("status", "normal");
        message.put("serverProgress", serverProgress);
        return message;
    }

    public JSONObject play(Long roomId, Long songId, Long progress, String songPayload) {
        JSONObject message = baseMessage("play", roomId);
        message.put("song", songPayload);
        message.put("progress", progress);
        message.put("songId", songId);
        message.put("playStatus", "1");
        return message;
    }

    public JSONObject pause(Long roomId, Long songId, Long progress) {
        JSONObject message = baseMessage("pause", roomId);
        message.put("songId", songId);
        message.put("progress", progress);
        return message;
    }

    public JSONObject seek(Long roomId, Long songId, Long progress) {
        JSONObject message = baseMessage("seek", roomId);
        message.put("songId", songId);
        message.put("progress", progress);
        return message;
    }

    public JSONObject stop(Long roomId) {
        return baseMessage("stop", roomId);
    }

    public JSONObject chat(Long roomId, UserSessionInfo userInfo, String content) {
        JSONObject message = baseMessage("chat", roomId);
        message.put("userId", userInfo.getUserId());
        message.put("nickname", userInfo.getNickname());
        message.put("avatar", userInfo.getAvatar());
        message.put("content", content);
        return message;
    }

    private JSONObject baseMessage(String action, Long roomId) {
        JSONObject message = new JSONObject();
        message.put("action", action);
        if (roomId != null) {
            message.put("roomId", roomId);
        }
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }
}
