package com.easy.pojo.model;

import lombok.Data;

@Data
public class UserSessionInfo {
    private Long roomId;
    private Long userId;
    private String nickname;
    private String avatar;
}
