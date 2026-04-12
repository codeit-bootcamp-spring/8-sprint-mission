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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 폼 로그인 성공 시 JWT(Access/Refresh) 발급과 응답 바디/쿠키 구성까지 담당하는 핸들러.
 * 기존 세션 기반의 LoginSuccessHandler를 대체한다.
 * 동시 로그인 제한을 위해 기존 토큰을 모두 폐기한 후 새 토큰을 발급한다.
 * Access Token은 응답 바디(JwtDto)로, Refresh Token은 HttpOnly 쿠키로 내려보낸다.
 */
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider tokenProvider;
  private final JwtSessionRegistry jwtSessionRegistry;
  private final UserMapper userMapper;


  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    System.out.println("[JwtLoginSuccessHandler] onAuthenticationSuccess 시작: 응답 구성 준비");

    // 응답 인코딩/콘텐츠 타입 설정
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // Principal 유효성 확인 및 캐스팅
    if (authentication.getPrincipal() instanceof DiscodeitUserDetails customUserDetails) {
      try {
        // 1. 동일 계정 기존 토큰 전부 무효화(동시 로그인 제한)
        System.out.println(
            "[JwtLoginSuccessHandler] 기존 토큰 무효화 시작 - username=" + customUserDetails.getUsername());
        jwtSessionRegistry.revokeAllByUsername(customUserDetails.getUsername());

        // 2. 새 Access/Refresh 발급
        System.out.println("[JwtLoginSuccessHandler] 새 토큰 발급 시작");
        String accessToken = tokenProvider.generateAccessToken(customUserDetails);
        String refreshToken = tokenProvider.generateRefreshToken(customUserDetails);

        // 3. 토큰 메타데이터 저장 (toEntity로 중복 제거)
        System.out.println("[JwtLoginSuccessHandler] 토큰 메타데이터 저장 시작");
        JwtTokenEntity accessEntity = tokenProvider.toEntity(accessToken);
        JwtTokenEntity refreshEntity = tokenProvider.toEntity(refreshToken);
        jwtSessionRegistry.register(accessEntity);
        jwtSessionRegistry.register(refreshEntity);

        // 4. 리프레시 쿠키 설정
        System.out.println("[JwtLoginSuccessHandler] 리프레시 쿠키 설정 시작");
        tokenProvider.addRefreshCookie(response, refreshToken);

        // 사용자 DTO 구성
        User user = customUserDetails.getUser();
        UserDto userDto = userMapper.toDto(user);

        // 5. JwtDto 바디 전송
        JwtDTO jwtDto = new JwtDTO(userDto, accessToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

        System.out.println("[JwtLoginSuccessHandler] onAuthenticationSuccess 완료: 응답 전송됨");
      } catch (Exception e) {
        // 예외 발생 시 처리(500)
        System.out.println("[JwtLoginSuccessHandler] 예외 발생: " + e.getMessage());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(objectMapper.createObjectNode().put("success", false)
            .put("message", "Token generation failed").toString());
      }
    } else {
      // 인증 실패 시 처리(401)
      System.out.println(
          "[JwtLoginSuccessHandler] Invalid principal: " + authentication.getPrincipal());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(
          objectMapper.createObjectNode().put("success", false).put("message", "Invalid principal")
              .toString());
    }
  }
}


