package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.interceptor.JwtAuthenticationChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    private final JwtAuthenticationChannelInterceptor jwtAuthenticationChannelInterceptor;

    private AuthorizationChannelInterceptor authorizationChannelInterceptor() {
        return new AuthorizationChannelInterceptor(
                MessageMatcherDelegatingAuthorizationManager.builder()
                        .anyMessage().hasRole(Role.USER.name())
                        .build()
        );
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("[WebSocket 설정] 메시지 브로커 설정 시작");

        config.enableSimpleBroker("/sub");
        config.setApplicationDestinationPrefixes("/pub");

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
        log.debug("[STOMP 엔드포인트] 경로: /ws, SockJS 폴백: 활성화, CORS: 모든 오리진 허용");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
                jwtAuthenticationChannelInterceptor, // 1순위
                new SecurityContextChannelInterceptor(), // 2순위
                authorizationChannelInterceptor() // 3순위
        );
    }
}
