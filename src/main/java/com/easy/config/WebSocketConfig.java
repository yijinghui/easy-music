
package com.easy.config;

import jakarta.websocket.server.ServerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置
 */
@Configuration
public class WebSocketConfig{

    @Bean
    // @ConditionalOnBean(ServerContainer.class)  // 只在有 ServerContainer 时才创建
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
