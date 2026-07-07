package com.easy.pojo.model;


import lombok.Data;

@Data
public class ChatMessage {
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private String createTime;
}
