package com.easy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.easy.mapper")
public class EasyMusicApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyMusicApplication.class, args);
    }

}
