package com.sprint.mission.discodeit.auth.handler;

import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.domain.user.event.UserLogInOutEvent;
import com.sprint.mission.discodeit.global.secutiry.JwtRegistry;
import com.sprint.mission.discodeit.global.secutiry.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class JwtLogoutHandler implements LogoutHandler
{
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserDetailsService userDetailsService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Cookie refreshTokenCookie = jwtTokenProvider.generateRefreshTokenExpirationCookie();
        response.addCookie(refreshTokenCookie);

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
                    .findFirst()
                    .ifPresent(cookie -> {
                        String refreshToken = cookie.getValue();
                        UUID userId = jwtTokenProvider.getUserId(refreshToken);
                        jwtRegistry.invalidateJwtInformationByUserId(userId);

                        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
                        DiscodeitUserDetails discodeitUserDetails = (DiscodeitUserDetails) userDetailsService.loadUserByUsername(username);
                        applicationEventPublisher.publishEvent(new UserLogInOutEvent(
                                userId,
                                false
                        ));
                    });
        }
    }
}
