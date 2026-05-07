package com.sprint.mission.discodeit.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    log.info("JWT 로그아웃 처리 시작");

    // REFRESH_TOKEN 쿠키를 찾아서 빈 쿠키로 덮어씌워서 삭제 처리
    if (request.getCookies() != null) {
      Arrays.stream(request.getCookies())
          .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
          .findFirst()
          .ifPresent(cookie -> {
            // Registry에서 토큰 정보 삭제
            jwtRegistry.invalidateJwtInformationByRefreshToken(cookie.getValue());
            // 쿠키 만료
            response.addHeader(HttpHeaders.SET_COOKIE,
                jwtTokenProvider.buildExpiredRefreshTokenCookie().toString());
          });
    }

    log.info("JWT 로그아웃 처리 완료");
  }
}
