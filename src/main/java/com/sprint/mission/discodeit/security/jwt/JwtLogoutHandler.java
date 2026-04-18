package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    if (request.getCookies() != null) {
      Optional<Cookie> refreshTokenCookie = Arrays.stream(request.getCookies())
          .filter(cookie -> JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
          .findFirst();

      refreshTokenCookie.ifPresent(cookie -> {
        try {
          String refreshToken = cookie.getValue();

          SignedJWT signedJWT = SignedJWT.parse(refreshToken);

          JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

          String userIdString = claimsSet.getStringClaim("userId");

          if (!userIdString.isEmpty()) {
            UUID userId = UUID.fromString(userIdString);

            jwtRegistry.invalidateJwtInformationByUserId(userId);
          }
        } catch (Exception e) {
          throw new DiscodeitException(ErrorCode.INVALID_TOKEN);
        }
      });
    }

    jwtTokenProvider.expireRefreshCookie(response);
  }
}
