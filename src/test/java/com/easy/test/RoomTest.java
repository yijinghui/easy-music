package com.easy.test;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void initPlayStatus() {
//        String key = "room:play:1";
//
//        stringRedisTemplate.opsForHash().put(key, "startTime", String.valueOf(System.currentTimeMillis()));
//        stringRedisTemplate.opsForHash().put(key, "progress", "0");
//        stringRedisTemplate.opsForHash().put(key, "playStatus", "1");
//        stringRedisTemplate.opsForHash().put(key, "songId", "1");

        stringRedisTemplate.opsForSet().add("rooms:online:", "1");
        stringRedisTemplate.opsForSet().add("rooms:online:", "2");
        stringRedisTemplate.opsForSet().add("rooms:online:", "3");
        stringRedisTemplate.opsForSet().add("rooms:online:", "4");

        System.out.println("初始化成功");
    }
}
