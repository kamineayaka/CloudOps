package com.cloudops.common.config;

import com.cloudops.common.security.WebSocketAuthHandshakeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final java.util.List<WebSocketHandler> handlers;
    private final WebSocketAuthHandshakeInterceptor authInterceptor;

    public WebSocketConfig(
            java.util.List<WebSocketHandler> handlers,
            WebSocketAuthHandshakeInterceptor authInterceptor) {
        this.handlers = handlers;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        handlers.forEach(handler -> {
            WebSocketEndpoint endpoint = handler.getClass().getAnnotation(WebSocketEndpoint.class);
            if (endpoint != null) {
                registry.addHandler(handler, endpoint.value())
                        .addInterceptors(new HttpSessionHandshakeInterceptor(), authInterceptor)
                        .setAllowedOriginPatterns("*");
            }
        });
    }

    @Bean
    public ServletServerContainerFactoryBean websocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(512 * 1024);
        container.setMaxBinaryMessageBufferSize(512 * 1024);
        container.setMaxSessionIdleTimeout(300_000L);
        return container;
    }
}
