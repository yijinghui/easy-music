package com.easy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class Knife4jConfig {


    /**
     * OpenAPI 3 配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        log.info("开始加载 Knife4j OpenAPI 3 文档");

        return new OpenAPI()
                .info(new Info()
                        .title("easy-music系统接口文档")
                        .description("easy-music系统接口文档")
                        .version("1.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }

    /**
     * 管理员模块分组
     */
    @Bean
    public GroupedOpenApi adminModuleApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .packagesToScan("com.easy.controller.admin")
                .build();
    }

    /**
     * 用户模块分组
     */
    @Bean
    public GroupedOpenApi userModuleApi() {
        return GroupedOpenApi.builder()
                .group("user")                      // 分组名称（UI左上角下拉框显示）
                .packagesToScan("com.easy.controller.user")  // 扫描指定包下的接口
                .build();
    }


}
