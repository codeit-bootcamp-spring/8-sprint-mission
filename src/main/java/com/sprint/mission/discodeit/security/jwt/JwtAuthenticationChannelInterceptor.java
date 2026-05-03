package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.exception.user.InvalidTokenException;
import com.sprint.mission.discodeit.service.basic.DiscodeitUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider tokenProvider;
  private final JwtRegistry jwtRegistry;
  private final DiscodeitUserDetailsService discodeitUserDetailsService;

  @Override
  public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);
    // 클라이언트가 처음 웹소켓 연결을 시도하기 위한 CONNECT 명령어일 때
    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      // Authorization 헤더 추출
      String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

      // 헤더의 Authorization 토큰 추출
      String token = null;
      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        token = authorizationHeader.substring(7);
      }

      try {
        // 토큰 유효성 검증
        if (StringUtils.hasText(token)) {
          if (tokenProvider.validateAccessToken(token)
              && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {

            String username = tokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = discodeitUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

            accessor.setUser(authentication);
            log.info("[JwtAuthenticationChannelInterceptor] 웹소켓 사용자 인증 성공 - 사용자: {}", username);
          } else {
            log.warn("[JwtAuthenticationChannelInterceptor] 웹소켓 사용자 인증 실패 - 유효하지 않은 토큰입니다.");
            throw new InvalidTokenException();
          }
        }
      } catch (Exception e) {
        log.error("[JwtAuthenticationChannelInterceptor] 웹소켓 사용자 인증 중 오류 발생 - {}", e.getMessage());
        throw new InvalidTokenException();
      }
    }
    return message;
  }
}
