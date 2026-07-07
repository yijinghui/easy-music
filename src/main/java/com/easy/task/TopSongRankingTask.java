package com.easy.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Arrays;


@Component
@Slf4j
public class TopSongRankingTask {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> RANK_SCRIPT;
    static {
        RANK_SCRIPT = new DefaultRedisScript<>();
        RANK_SCRIPT.setLocation(new ClassPathResource("script/rank.lua"));
        RANK_SCRIPT.setResultType(Long.class);
    }


    @Scheduled(cron = "0 0 2 ? * 1")
    void buildTopSongWeeklyRankTask(){
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

    @Scheduled(cron = "0 0 2 1 * ?")
    void buildTopSongMonthlyRankTask(){


        log.info("更新月排行榜");
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        int year = lastWeek.getYear();
        int month = lastWeek.getMonthValue();
        int ttl = 5184000;
        int count = 300;

        String oldKey = "top:monthly:" + year + "-" + month;
        String newKey = "top:monthly:now";
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
