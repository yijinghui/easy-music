package com.easy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisProperties {
    private String host = "redis";
    private int port = 6379;
    private String password;
    private int database = 1;
}
