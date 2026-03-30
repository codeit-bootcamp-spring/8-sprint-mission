package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    int status = HttpServletResponse.SC_UNAUTHORIZED;

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),                                // timestamp
        ErrorCode.INVALID_CREDENTIALS.name(),       // code (상수 이름: "INVALID_CREDENTIALS")
        ErrorCode.INVALID_CREDENTIALS.getMessage(), // message ("아이디 또는 비밀번호가 올바르지 않습니다.")
        Map.of("path", request.getRequestURI()),  // details: 요청 경로 등 맥락값
        exception.getClass().getSimpleName(),          // 발생한 예외 클래스 명
        status                                         // status (401)
    );

    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}
