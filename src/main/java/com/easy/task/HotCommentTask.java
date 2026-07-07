package com.easy.task;


import com.easy.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HotCommentTask {

    @Autowired
    CommentService commentService;


    @Scheduled(cron = "0 0/1 * * * ?")
    void refreshHotCommentTask(){
        commentService.refreshHotComment();
    }
}
