package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;


  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException {

    // UserDetails에서 UserDto 추출 -> 토큰 만들때 필요한 권한과 유저 ID 뽑아오기 위해서
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();
    UserDto userDto = userDetails.getUserDto();

    // userId와 role로 액세스, 리프레시 토큰 발급
    String accessToken = jwtTokenProvider.createAccessToken(userDto.id(), userDto.role().name());
    String refreshToken = jwtTokenProvider.createRefreshToken(userDto.id(), userDto.role().name());

    // 브라우저에 보낼 리프레시 토큰에 대한 쿠키 설정
    Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
    // 오직 HTTP 통신에만 쿠키 실어보냄, 임의의 JS 접근 차단
    refreshCookie.setHttpOnly(true);
    // 쿠키 전송할 요청 범위
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(604800);
    // 응답에 커스텀하게 만든 쿠키를 넣어놓는다.
    response.addCookie(refreshCookie);

    JwtDto jwtDto = new JwtDto(accessToken, userDto);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(response.getWriter(), jwtDto);
  }
}
