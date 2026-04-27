package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.SseService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final UserRepository userRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final SseService sseService;

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

            userRepository.findById(userId).ifPresent(user -> {
                  UserDto offlineUserDto = new UserDto(
                      user.getId(),
                      user.getUsername(),
                      user.getEmail(),
                      binaryContentMapper.toDto(user.getProfile()),
                      false,
                      user.getRole()
                  );

                  try {
                    sseService.broadcast(
                        "users.updated",
                        offlineUserDto
                    );
                  } catch (Exception e) {
                    log.warn("[JwtLogoutHandler] 실시간 알림 전송 실패 - 사유: {}", e.getMessage());
                  }
                }
            );

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
