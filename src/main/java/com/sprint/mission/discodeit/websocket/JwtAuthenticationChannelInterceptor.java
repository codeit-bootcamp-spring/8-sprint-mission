package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.jwt.JwtTokenProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailsService userDetailsService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
        message,
        StompHeaderAccessor.class
    );

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String authorizationHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

      if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        try {
          if (jwtTokenProvider.validateAccessToken(token)) {
            String userIdString = jwtTokenProvider.getUserIdFromToken(token);
            UUID userId = UUID.fromString(userIdString);
            UserDetails userDetails = userDetailsService.loadUserByUserId(userId);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

            accessor.setUser(authentication);
          } else {
            log.warn("유효하지 않은 JWT 토큰");
            throw new IllegalArgumentException("Invalid JWT token");
          }
        } catch (Exception e) {
          log.error("WebSocket 인증 실패: {}", e.getMessage());
          throw new IllegalArgumentException("Authentication failed", e);
        }
      } else {
        log.warn("Authorization 헤더가 없거나 형식이 올바르지 않음");
        throw new IllegalArgumentException("Missing or invalid Authorization header");
      }
    }

    return message;
  }
}
