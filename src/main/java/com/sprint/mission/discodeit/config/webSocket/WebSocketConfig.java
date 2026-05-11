package com.sprint.mission.discodeit.config.webSocket;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.jwt.JwtAuthenticationChannelInterceptor;
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
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    private final JwtAuthenticationChannelInterceptor jwtAuthenticationChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/pub");
        config.setUserDestinationPrefix("/sub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25000)       // 25초마다 하트비트 전송
                .setDisconnectDelay(5000);   // 5초 후 연결 해제
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(4)       // 기본 스레드 수
                .maxPoolSize(8)         // 최대 스레드 수
                .queueCapacity(100);  // 대기 큐 용량

        registration.interceptors(
                jwtAuthenticationChannelInterceptor,
                new SecurityContextChannelInterceptor(),
                authorizationChannelInterceptor()
        );
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(4)       // 기본 스레드 수
                .maxPoolSize(8)         // 최대 스레드 수
                .queueCapacity(100);  // 대기 큐 용량
    }

    private AuthorizationChannelInterceptor authorizationChannelInterceptor() {
        return new AuthorizationChannelInterceptor(
                MessageMatcherDelegatingAuthorizationManager.builder()
                        .anyMessage().hasRole(Role.USER.name())
                        .build()
        );
    }
}