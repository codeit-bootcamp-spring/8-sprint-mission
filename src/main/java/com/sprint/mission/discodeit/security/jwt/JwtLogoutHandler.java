package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.cache.CacheNames;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtRegistry jwtRegistry;
    private final CacheManager cacheManager;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
                    .findFirst()
                    .ifPresent(cookie -> {
                        String refreshToken = cookie.getValue();
                        jwtRegistry.invalidateJwtInformationByRefreshToken(refreshToken);

                        // 쿠키 삭제 및 Secure/SameSite 속성 동일하게 지정
                        ResponseCookie deleteCookie = ResponseCookie.from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, "")
                                .httpOnly(true)
                                .secure(true) // 삭제 시에도 속성을 맞춰주는게 좋습니다.
                                .sameSite("Strict")
                                .path("/")
                                .maxAge(0)
                                .build();

                        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
                    });
        }
        var usersCache = cacheManager.getCache(CacheNames.USERS_ALL);
        if (usersCache != null) {
            usersCache.clear();
        }
    }
}
