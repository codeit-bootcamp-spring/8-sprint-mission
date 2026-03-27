package com.sprint.mission.discodeit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    String errorCode;
    String errorMessage;

    if (exception instanceof BadCredentialsException) {
      errorCode = "INVALID_CREDENTIALS";
      errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
    } else if (exception instanceof DisabledException) {
      errorCode = "ACCOUNT_DISABLED";
      errorMessage = "비활성화된 계정입니다.";
    } else if (exception instanceof LockedException) {
      errorCode = "ACCOUNT_LOCKED";
      errorMessage = "잠긴 계정입니다.";
    } else {
      errorCode = "LOGIN_FAILED";
      errorMessage = "로그인에 실패했습니다.";
    }

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        errorCode,
        errorMessage,
        null,
        exception.getClass().getSimpleName(),
        HttpServletResponse.SC_UNAUTHORIZED
    );

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
