package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.event.UserPresenceChangedEvent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * 로그아웃 시 리프레시 토큰에 대한 쿠키를 삭제한다.
 */
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtRegistry jwtRegistry;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    if (request.getCookies() != null) {
      Optional<Cookie> refreshCookieOpt = Arrays.stream(request.getCookies())
          .filter(cookie -> cookie.getName().equals("REFRESH_TOKEN"))
          .findFirst();

      refreshCookieOpt.ifPresent(refreshCookie -> {
        String refreshToken = refreshCookie.getValue();

        // 무효화 전에 사용자 정보 조회
        var maybeInfo = jwtRegistry.findJwtInformationByRefreshToken(refreshToken);

        jwtRegistry.invalidateJwtInformationByRefreshToken(refreshToken);

        // 해당 사용자가 완전히 오프라인이면 상태 브로드캐스트
        maybeInfo.ifPresent(info -> {
          UserDto dto = info.getUserDto();
          if (!jwtRegistry.hasActiveJwtInformationByUserId(dto.id())) {
            UserDto offlineDto = new UserDto(dto.id(), dto.username(), dto.email(), dto.profile(), false, dto.role());
            eventPublisher.publishEvent(new UserPresenceChangedEvent(offlineDto));
          }
        });
      });
    }

    Cookie cookie = new Cookie("REFRESH_TOKEN", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }
}
