package com.sprint.mission.discodeit.global.secutiry;

import com.sprint.mission.discodeit.global.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor
{
    private static final String AUTHENTICATION_HEADER = "Authorization";
    private static final String AUTHENTICATION_SCHEME_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtRegistry jwtRegistry;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        try {
            String token = extractToken(accessor);

            if (token == null) {
                throw new InvalidTokenException("Invalid token");
            }

            if (jwtTokenProvider.validateAccessToken(token) && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                accessor.setUser(authenticationToken);
            } else {
                throw new InvalidTokenException("Invalid token");
            }
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid token");
        }


        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader(AUTHENTICATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTHENTICATION_SCHEME_PREFIX)) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
