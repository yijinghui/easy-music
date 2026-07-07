package com.easy.test;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Arrays;

@SpringBootTest
@Slf4j
public class RankInitTest {


    private static final DefaultRedisScript<Long> RANK_SCRIPT;
    static {
        RANK_SCRIPT = new DefaultRedisScript<>();
        RANK_SCRIPT.setLocation(new ClassPathResource("script/rank.lua"));
        RANK_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    @Test
    public void weekRankTest() {
        String key = "top:weekly:now";
        // 添加300条数据
        for (int i = 2; i <= 301; i++) {
            stringRedisTemplate.opsForZSet().add(key, i + "", 0);
        }

    }

    @Test
    public void monthRankTest() {
        String key = "top:monthly:now";
        // 添加300条数据
        for (int i = 2; i <= 301; i++) {
            stringRedisTemplate.opsForZSet().add(key, i + "", 0);
        }

    }

    @Test
    public void updateWeekRank() {
        log.info("更新周排行榜");
        // 获取上一周（使用WeekFields.ISO，周一为一周的开始）
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        int year = lastWeek.getYear();
        int week = lastWeek.get(WeekFields.ISO.weekOfWeekBasedYear());
        int ttl = 5184000;
        int count = 300;

        String oldKey = "top:weekly:" + year + "-" + week;
        String newKey = "top:weekly:now";
        // 执行归档操作
        long result = stringRedisTemplate.execute(
                RANK_SCRIPT,
                Arrays.asList(oldKey, newKey),
                String.valueOf(count), String.valueOf(ttl)
        );
        // 检查归档结果
        if (result == 0) {
            log.info("更新榜单失败，没有数据可归档");
        }
    }
}
