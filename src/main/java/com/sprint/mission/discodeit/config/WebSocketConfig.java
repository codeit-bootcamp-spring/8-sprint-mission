package com.sprint.mission.discodeit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final WebSocketInterceptor webSocketInterceptor;

    public WebSocketConfig(WebSocketInterceptor webSocketInterceptor) {
        this.webSocketInterceptor = webSocketInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("[WebSocket 설정] 메시지 브로커 설정 시작");

        config.enableSimpleBroker(
                // "/topic"으로 시작하는 경로는 브로커가 처리하여 구들자들에게 메시지를 브로드캐스트함.
                // 모든 구독자에게 메시지 전송
                "/topic",
                // "/queue"는 개별 사용자를 위한 메시지 대기열이다.
                "/queue"
        );
        log.debug("[메시지 브로커] 구독 경로 설정: /topic (브로드캐스트), /queue (개별 메시지)");

        config.setApplicationDestinationPrefixes("/pub");
        log.debug("[메시지 브로커] 애플리케이션 목적지 접두사 설정: /pub");
        config.setUserDestinationPrefix("/sub");
        log.debug("[메시지 브로커] 사용자별 메시지 경로 접두사 설정: /sub");

        log.info("[WebSocket 설정] 메시지 브로커 설정 완료");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        log.info("[WebSocket 설정] STOMP 엔드포인트 등록 시작");

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25000)
                .setDisconnectDelay(5000);

        log.info("[WebSocket 설정] STOMP 엔드포인트 등록 완료");
        log.debug("[STOMP 엔드포인트] 경로: /chat, SockJS 폴백: 활성화, CORS: 모든 오리진 허용");
    }
}
