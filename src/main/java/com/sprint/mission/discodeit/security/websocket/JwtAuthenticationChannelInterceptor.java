package com.sprint.mission.discodeit.security.websocket;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.enums.AuthErrorCode;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final UserDetailsService userDetailsService;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);

    // 웹소켓 최초 연결(CONNECT) 요청일 때 검사
    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        String token = authorizationHeader.substring(7);

        // 토큰 유효성, 활성화 상태 검증
        if (jwtTokenProvider.validateToken(token)
            && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {

          // 토큰에서 유저명 추출 후 UserDetails 객체 생성
          String username = jwtTokenProvider.extractUsername(token);
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);

          // Spring Security용 인증 객체 생성
          Authentication authentication = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );

          // STOMP에 인증 객체 저장
          accessor.setUser(authentication);
          log.info("웹 소켓 인증 성공: {}", authentication.getName());
        } else {
          log.warn("웹 소켓 인증 실패: 유효하지 않은 토큰");
          throw new DiscodeitException(AuthErrorCode.INVALID_TOKEN);
        }
      } else {
        log.warn("웹 소켓 인증 실패: Authorization 헤더 누락");
        throw new DiscodeitException(AuthErrorCode.INVALID_TOKEN);
      }
    }

    return message;
  }
}
