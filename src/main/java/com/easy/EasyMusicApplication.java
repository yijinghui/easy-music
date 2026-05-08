package com.easy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.easy.mapper")
@EnableCaching
public class EasyMusicApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyMusicApplication.class, args);
    }

}
