package com.easy.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlayState {

    private Long songId;
    private Long startTime;
    private Long progress;
    private Integer playStatus;
    private String song;
}
