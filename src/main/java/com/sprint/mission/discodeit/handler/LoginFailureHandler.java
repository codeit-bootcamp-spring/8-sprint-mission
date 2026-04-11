package com.sprint.mission.discodeit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import com.sprint.mission.discodeit.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    log.warn("[LoginFailureHandler]: 로그인 실패 - exception={}, message={}",
        exception.getClass().getSimpleName(), exception.getMessage());

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    ErrorCode errorCode;

    if (exception instanceof BadCredentialsException) {
      errorCode = ErrorCode.INVALID_CREDENTIALS;
    } else if (exception instanceof DisabledException) {
      errorCode = ErrorCode.ACCOUNT_DISABLED;
    } else if (exception instanceof LockedException) {
      errorCode = ErrorCode.ACCOUNT_LOCKED;
    } else {
      errorCode = ErrorCode.LOGIN_FAILED;
    }

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        errorCode.name(),
        errorCode.getMessage(),
        null,
        exception.getClass().getSimpleName(),
        HttpServletResponse.SC_UNAUTHORIZED
    );

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
