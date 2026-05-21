package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.cache.CacheNames;
import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final CacheManager cacheManager;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        DiscodeitUserDetails principal = (DiscodeitUserDetails) authentication.getPrincipal();
        UserDto userDto = principal.getUserDto();
        String username = userDto.username();

        String accessToken = jwtTokenProvider.createAccessToken(username);
        String refreshToken = jwtTokenProvider.createRefreshToken(username);

        java.time.Instant accessExpiry = jwtTokenProvider.getExpirationTimeFromToken(accessToken);
        java.time.Instant refreshExpiry = jwtTokenProvider.getExpirationTimeFromToken(refreshToken);

        // 등록 (동일 user 로그인 시 과거 토큰 무효화)
        JwtInformation jwtInfo = new JwtInformation(userDto, accessToken, refreshToken, accessExpiry, refreshExpiry);
        jwtRegistry.registerJwtInformation(jwtInfo);

        var usersCache = cacheManager.getCache(CacheNames.USERS_ALL);
        if (usersCache != null) {
            try {
                usersCache.clear();
            } catch (RuntimeException e) {
                log.warn("사용자 목록 캐시 무효화 실패(백엔드 캐시/Redis 연결 확인): {}", e.getMessage());
            }
        }

        ResponseCookie refreshTokenCookie = ResponseCookie.from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Strict") // CSRF 공격 방지
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days in seconds
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        JwtDto jwtDto = new JwtDto(userDto, accessToken);
        objectMapper.writeValue(response.getWriter(), jwtDto);
    }
}
