package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.enums.Role;
import com.sprint.mission.discodeit.security.websocket.JwtAuthenticationChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authorization.AuthorizationManager;
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

  private final JwtAuthenticationChannelInterceptor jwtAuthenticationChannelInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {

    // 메모리 기반 SimpleBroker 사용
    // 클라이언트에서 메시지 구독 시 prefix
    config.enableSimpleBroker("/sub");
    // 클라이언트에서 메시지 발행 시 prefix
    config.setApplicationDestinationPrefixes("/pub");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {

    // STOMP 엔드포인트 /ws 설정, SockJS 연결 지원
    registry.addEndpoint("/ws")
        .withSockJS();
  }

  // 클라이언트가 보내는 메시지(Inbound)가 지나가는 채널에 보안 인터셉터 추가
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
        jwtAuthenticationChannelInterceptor,    // 1차. 연결(CONNECT) 시 토큰 검증 및 User 세팅
        new SecurityContextChannelInterceptor(),// 2차. 세팅된 User를 SecurityContext에 담아줌
        authorizationChannelInterceptor()       // 3차. 권한이 있는지 최종 인가 확인
    );
  }

  // 위 메서드의 3차 권한 인가 정책 정의. 어떤 메시지를 누가 보낼 수 있는지 결정
  private AuthorizationChannelInterceptor authorizationChannelInterceptor() {
    AuthorizationManager<Message<?>> authorizationManager =
        MessageMatcherDelegatingAuthorizationManager.builder()
            // 모든 메시지 전송은 최소 USER 권한이 있어야함
            .anyMessage().hasRole(Role.USER.name())
            .build();

    return new AuthorizationChannelInterceptor(authorizationManager);
  }
}
