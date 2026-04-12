package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.security.store.JwtSessionRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider tokenProvider;
  private final JwtSessionRegistry jwtSessionRegistry;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    String authz = request.getHeader("Authorization");
    if (authz != null && authz.startsWith("Bearer ")) {
      String at = authz.substring(7);
      try {
        String atJti = tokenProvider.getTokenId(at);

        jwtSessionRegistry.revokeByJti(atJti);
      } catch (Exception ignored) {
      }
    }

    if (request.getCookies() != null) {
      Optional<Cookie> rtCookie = Arrays.stream(request.getCookies())
          .filter(c -> JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(c.getName()))
          .findFirst();
      rtCookie.ifPresent(c -> {
        try {
          String rtJti = tokenProvider.getTokenId(c.getValue());
          jwtSessionRegistry.revokeByJti(rtJti);
        } catch (Exception ignored) {
        }
      });
    }

    tokenProvider.expireRefreshCookie(response);

  }
}


