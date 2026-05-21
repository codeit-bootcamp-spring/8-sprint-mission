package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor, Ordered {

	private final JwtTokenProvider jwtTokenProvider;
	private final DiscodeitUserDetailsService userDetailsService;
	private final JwtRegistry jwtRegistry;

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}
		if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
			return message;
		}

		String token = resolveBearerToken(accessor);
		if (token == null) {
			log.warn("STOMP CONNECT 에 Authorization Bearer 토큰이 없습니다.");
			throw new AccessDeniedException("Missing Authorization token");
		}
		if (!jwtTokenProvider.validateToken(token)) {
			log.warn("STOMP CONNECT JWT 검증 실패");
			throw new AccessDeniedException("Invalid JWT");
		}
		if (!jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
			log.warn("STOMP CONNECT JWT 가 활성 세션에 없습니다.");
			throw new AccessDeniedException("Inactive or revoked JWT");
		}

		String username = jwtTokenProvider.getUsernameFromToken(token);
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				userDetails.getAuthorities()
		);
		accessor.setUser(authentication);
		log.debug("STOMP CONNECT 인증 완료, user={}", username);
		return message;
	}

	private static String resolveBearerToken(StompHeaderAccessor accessor) {
		String raw = accessor.getFirstNativeHeader("Authorization");
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String v = raw.trim();
		if (!v.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return null;
		}
		return v.substring(7).trim();
	}
}
