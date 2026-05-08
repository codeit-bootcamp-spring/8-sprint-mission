package com.sprint.mission.discodeit.interceptor;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final RoleHierarchy roleHierarchy;
    private final JwtRegistry jwtRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class
        );

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = resolveToken(accessor)
                    .orElseThrow(() -> new RuntimeException("INVALID_TOKEN"));

            // HTTP 필터와 동일한 로직: 토큰 검증 + JWT 세션 확인
            if (tokenProvider.validateAccessToken(token)
                    && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {

                DiscodeitUserDetails userDetails = tokenProvider.parseAccessToken(token);
                UserDto userDTO = userDetails.getUserDto();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                roleHierarchy.getReachableGrantedAuthorities(
                                        userDetails.getAuthorities()
                                )
                        );

                accessor.setUser(authentication);
                log.debug("Set authentication for websocket user: {}", userDTO.username());
            } else {
                log.debug("Invalid JWT token for websocket connect");
                throw new RuntimeException("INVALID_TOKEN");
            }
        }

        return message;
    }

    private Optional<String> resolveToken(StompHeaderAccessor accessor) {
        String prefix = "Bearer ";

        String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(prefix)) {
            return Optional.of(authHeader.substring(prefix.length()));
        }

        String accessTokenHeader = accessor.getFirstNativeHeader("ACCESS_TOKEN");
        if (StringUtils.hasText(accessTokenHeader)) {
            return Optional.of(accessTokenHeader);
        }

        return Optional.empty();
    }
}