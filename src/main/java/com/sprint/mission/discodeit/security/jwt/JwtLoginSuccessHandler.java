package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final JwtProperties jwtProperties;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {

    // 인증된 유저 정보 추출
    if (!(authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails)) {
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      return;
    }

    log.info("JWT 로그인 시작");

    UserResponse userResponse = userDetails.getUserResponse();
    UUID userId = userResponse.id();
    String username = userDetails.getUsername();

    // Provider로 토큰 발급
    String accessToken = jwtTokenProvider.generateAccessToken(userId, username);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

    // 토큰 만료시간
    Instant now = Instant.now();
    Instant accessExpiry = now.plusMillis(jwtProperties.accessTokenExpiry());
    Instant refreshExpiry = now.plusMillis(jwtProperties.refreshTokenExpiry());

    // JwtInformation 객체 등록
    JwtInformation jwtInformation = new JwtInformation(userResponse, accessToken, refreshToken,
        accessExpiry, refreshExpiry);
    jwtRegistry.registerJwtInformation(jwtInformation);

    // refresh 토큰 ResponseCookie로 생성 후 헤더 추가
    response.addHeader(HttpHeaders.SET_COOKIE,
        jwtTokenProvider.buildRefreshTokenCookie(refreshToken).toString());

    // access 토큰 + 유저 정보 -> dto
    JwtDto jwtDto = new JwtDto(userResponse, accessToken);

    response.setStatus(HttpStatus.OK.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), jwtDto);

    log.info("JWT 로그인 성공 : {}", username);
  }

}
