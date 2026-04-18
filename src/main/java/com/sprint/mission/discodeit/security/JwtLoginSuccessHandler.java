package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.auth.dto.JwtDTO;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.security.store.JwtSessionRegistry;
import com.sprint.mission.discodeit.security.store.JwtTokenEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 로그인 성공 후 JWT(Access/Refresh) 발급과 응답 바디/쿠키 구성을 담당하는 핸들러.
 * 기존 세션 기반 LoginSuccessHandler를 대체한다.
 * 동시 로그인을 허용하지 않기 위해 기존 토큰을 모두 폐기한 뒤 새 토큰을 발급한다.
 * Access Token은 응답 바디(JwtDto)로, Refresh Token은 HttpOnly 쿠키로 내려보낸다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider tokenProvider;
  private final JwtSessionRegistry jwtSessionRegistry;
  private final UserMapper userMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    log.info("[JwtLoginSuccessHandler] onAuthenticationSuccess 시작: 응답 구성 준비");

    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    if (authentication.getPrincipal() instanceof DiscodeitUserDetails customUserDetails) {
      try {
        log.info("[JwtLoginSuccessHandler] 기존 토큰 무효화 시작 - username={}",
            customUserDetails.getUsername());
        jwtSessionRegistry.revokeAllByUsername(customUserDetails.getUsername());

        log.info("[JwtLoginSuccessHandler] 새 토큰 발급 시작");
        String accessToken = tokenProvider.generateAccessToken(customUserDetails);
        String refreshToken = tokenProvider.generateRefreshToken(customUserDetails);

        log.info("[JwtLoginSuccessHandler] 토큰 메타데이터 저장 시작");
        JwtTokenEntity accessEntity = tokenProvider.toEntity(accessToken);
        JwtTokenEntity refreshEntity = tokenProvider.toEntity(refreshToken);
        jwtSessionRegistry.register(accessEntity);
        jwtSessionRegistry.register(refreshEntity);

        log.info("[JwtLoginSuccessHandler] 리프레시 쿠키 설정 시작");
        tokenProvider.addRefreshCookie(response, refreshToken);

        User user = customUserDetails.getUser();
        UserDto userDto = userMapper.toDto(user);

        JwtDTO jwtDto = new JwtDTO(userDto, accessToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

        log.info("[JwtLoginSuccessHandler] onAuthenticationSuccess 완료: 응답 전송됨");
      } catch (Exception e) {
        log.error("[JwtLoginSuccessHandler] 예외 발생", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(objectMapper.createObjectNode().put("success", false)
            .put("message", "Token generation failed").toString());
      }
    } else {
      log.warn("[JwtLoginSuccessHandler] Invalid principal: {}", authentication.getPrincipal());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(
          objectMapper.createObjectNode().put("success", false).put("message", "Invalid principal")
              .toString());
    }
  }
}
