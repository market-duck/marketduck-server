package com.devgang.marketduck.config;

import com.devgang.marketduck.auth.filter.CorsFilter;
import com.devgang.marketduck.config.websocket.AuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독하는 요청 prefix
        registry.enableSimpleBroker("/sub");

        // 메시지를 발행하는 요청 prefix
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 웹소켓 연결 엔드포인트 설정
        registry.addEndpoint("/ws-chat")
                // CORS 필터와 충돌하지 않도록 setAllowedOriginPatterns를 사용하지 않음
                // CorsFilter에서 Origin 처리를 통합 관리함
                .addInterceptors(authHandshakeInterceptor)
                .withSockJS()
                .setHeartbeatTime(15000) // 15초 간격으로 SockJS 하트비트
                .setDisconnectDelay(30000); // 30초 후 연결 종료로 간주
    }
}