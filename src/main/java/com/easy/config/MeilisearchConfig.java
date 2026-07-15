package com.easy.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeilisearchConfig {

    @Bean
    @ConfigurationProperties(prefix = "meilisearch")
    public MeilisearchProperties meilisearchProperties(){
        return new MeilisearchProperties();
    }

    @Bean
    public Client meilisearchClient(MeilisearchProperties meilisearchProperties){
        return new Client(new Config(meilisearchProperties.getHostUrl(), meilisearchProperties.getMasterKey()));
    }

}
