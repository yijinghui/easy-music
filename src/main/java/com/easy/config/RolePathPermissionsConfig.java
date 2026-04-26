package com.easy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "role-path-permissions")
@Data
// @ConfigurationProperties：
// 这个注解告诉 Spring：把配置文件中以 role-path-permissions 开头的所有配置项，自动赋值给这个类的对应字段。
public class RolePathPermissionsConfig {

    private Map<String, List<String>> permissions;

}
