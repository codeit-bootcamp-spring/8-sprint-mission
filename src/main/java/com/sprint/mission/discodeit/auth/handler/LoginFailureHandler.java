package com.sprint.mission.discodeit.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.auth.constants.AuthConstants;
import com.sprint.mission.discodeit.auth.dto.AuthErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException, ServletException {
    String errorMessage = determineErrorMessage(exception);

    log.warn("{} 로그인 실패: {}", AuthConstants.LOG_PREFIX_LOGIN_FAILURE, errorMessage, exception);

    AuthErrorResponse errorResponse = new AuthErrorResponse(
        AuthConstants.ERROR_AUTHENTICATION_FAILED,
        errorMessage
    );

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }

  /**
   * 예외 타입에 따른 에러 메시지 결정
   */
  private String determineErrorMessage(AuthenticationException exception) {
    if (exception instanceof BadCredentialsException) {
      return AuthConstants.MSG_INVALID_CREDENTIALS;
    } else if (exception instanceof DisabledException) {
      return AuthConstants.MSG_ACCOUNT_DISABLED;
    } else {
      return AuthConstants.MSG_LOGIN_FAILED;
    }
  }
}
