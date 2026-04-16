package com.sprint.mission.discodeit.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtRegistry jwtRegistry;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME)) {
          String refreshToken = cookie.getValue();
          jwtRegistry.invalidateJwtInformationByRefreshToken(refreshToken);
          log.info("로그아웃: 레지스트리에서 리프레시 토큰 무효화 완료");
          break;
        }
      }
    }

    Cookie cookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, null);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}
