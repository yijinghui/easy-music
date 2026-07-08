
-- 音乐歌房表
CREATE TABLE IF NOT EXISTS tb_music_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '歌房ID',
    room_name VARCHAR(50) NOT NULL COMMENT '歌房名称',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    current_song_id BIGINT DEFAULT NULL COMMENT '当前播放歌曲ID',
    current_progress BIGINT DEFAULT 0 COMMENT '当前播放进度（毫秒）',
    play_status TINYINT DEFAULT 0 COMMENT '播放状态：0-暂停，1-播放中',
    room_status TINYINT DEFAULT 0 COMMENT '歌房状态：0-正常，1-关闭',
    max_users INT DEFAULT 20 COMMENT '最大人数',
    current_users INT DEFAULT 0 COMMENT '当前人数',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    close_time DATETIME DEFAULT NULL COMMENT '关闭时间',
    INDEX idx_creator (creator_id),
    INDEX idx_status (room_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐歌房表';

-- 歌房成员表
CREATE TABLE IF NOT EXISTS tb_room_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成员ID',
    room_id BIGINT NOT NULL COMMENT '歌房ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    nickname VARCHAR(50) NOT NULL COMMENT '用户昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '用户头像',
    role TINYINT DEFAULT 0 COMMENT '角色：0-普通成员，1-管理员，2-创建者',
    join_time DATETIME NOT NULL COMMENT '加入时间',
    leave_time DATETIME DEFAULT NULL COMMENT '离开时间',
    online_status TINYINT DEFAULT 1 COMMENT '在线状态：0-离线，1-在线',
    INDEX idx_room (room_id),
    INDEX idx_user (user_id),
    UNIQUE KEY uk_room_user (room_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌房成员表';
