package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.security.store.JwtSessionRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;


@Component
@Slf4j
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

        log.info("[JwtLogoutHandler] 액세스 토큰 폐기 완료: jti={}", atJti);
      } catch (Exception exception) {
        log.error("[JwtLogoutHandler] Authorization 헤더의 액세스 토큰 폐기 중 오류가 발생했습니다.",
            exception);
      }
    } else {
      log.debug("[JwtLogoutHandler] Authorization 헤더에 Bearer 액세스 토큰이 없습니다.");
    }

    if (request.getCookies() != null) {
      Optional<Cookie> rtCookie = Arrays.stream(request.getCookies())
          .filter(c -> JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(c.getName()))
          .findFirst();
      rtCookie.ifPresent(c -> {
        try {
          String rtJti = tokenProvider.getTokenId(c.getValue());
          jwtSessionRegistry.revokeByJti(rtJti);
          log.info("[JwtLogoutHandler] 리프레시 토큰 폐기 완료: jti={}", rtJti);
        } catch (Exception exception) {
          log.error("[JwtLogoutHandler] 리프레시 토큰 폐기 중 오류가 발생했습니다.", exception);
        }
      });
    }

    tokenProvider.expireRefreshCookie(response);

  }
}

