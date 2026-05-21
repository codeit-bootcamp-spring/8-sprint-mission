package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider tokenProvider;
  private final RoleHierarchy roleHierarchy;
  private final UserDetailsService userDetailsService;
  private final JwtRegistry jwtRegistry;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);
    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = resolveToken(accessor)
          .orElseThrow(() -> new DiscodeitException(ErrorCode.INVALID_TOKEN));

      if (tokenProvider.validateAccessToken(token)
          && jwtRegistry.hasActiveJwtInformationByAccessToken(
          token)) {
        String username = tokenProvider.getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                roleHierarchy.getReachableGrantedAuthorities(userDetails.getAuthorities())
            );

        accessor.setUser(authentication);
        log.debug("Set authentication for user: {}", username);
      } else {
        log.debug("Invalid JWT token");
        throw new DiscodeitException(ErrorCode.INVALID_TOKEN);
      }
    }
    return message;
  }

  private Optional<String> resolveToken(StompHeaderAccessor accessor) {
    String prefix = "Bearer ";
    return Optional.ofNullable(accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION))
        .map(value -> {
          if (value.startsWith(prefix)) {
            return value.substring(prefix.length());
          } else {
            return null;
          }
        });
  }
}
