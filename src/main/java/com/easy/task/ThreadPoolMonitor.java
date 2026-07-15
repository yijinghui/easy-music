package com.easy.task;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class ThreadPoolMonitor {

    @Autowired
    private ThreadPoolTaskExecutor smsExecutor;

    @Scheduled(fixedDelay = 10000) // fixedDelay是从上一次任务执行结束开始计时，等10秒后再启动下一次
    public void monitor(){
        ThreadPoolExecutor threadPool = smsExecutor.getThreadPoolExecutor();
        log.info("短信线程池 - 活跃线程:{}, 队列大小:{}, 已完成任务:{}",
                threadPool.getActiveCount(),
                threadPool.getQueue().size(),
                threadPool.getCompletedTaskCount()
        );

        // 队列持续积压时告警
        if (threadPool.getQueue().size() > 200) {
            log.info("短信线程池队列积压严重");
        }
    }
}
