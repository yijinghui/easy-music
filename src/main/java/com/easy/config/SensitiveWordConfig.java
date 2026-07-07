package com.easy.config;

import org.springframework.context.annotation.Configuration;

/**
 * 敏感词配置类
 * 使用自定义的DFA算法和Trie树实现敏感词过滤
 */
@Configuration
public class SensitiveWordConfig {
    // 敏感词工具类已通过@Component自动扫描注册
    // 无需额外配置Bean
}
