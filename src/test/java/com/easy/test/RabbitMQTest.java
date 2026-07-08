package com.easy.test;


import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RabbitMQTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void test01(){
        String queue ="test.queue";
        String msg="hello,Spring amqp";
        rabbitTemplate.convertAndSend(queue,msg);
    }
    
}
