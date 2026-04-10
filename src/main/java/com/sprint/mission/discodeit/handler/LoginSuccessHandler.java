package com.sprint.mission.discodeit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import com.sprint.mission.discodeit.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    // JSON 응답 설정
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    if (authentication.getPrincipal() instanceof DiscodeitUserDetails discodeitUserDetails) {

      UserDto userDto = discodeitUserDetails.getUserDto();

      response.setStatus(HttpServletResponse.SC_OK);

      // 사용자 정보를 JSON으로 응답
      String responseBody = objectMapper.writeValueAsString(userDto);
      response.getWriter().write(responseBody);

    } else {

      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

      ErrorCode errorCode = ErrorCode.UNEXPECTED_PRINCIPAL_TYPE;

      ErrorResponse errorResponse = new ErrorResponse(
          Instant.now(),
          errorCode.name(),
          errorCode.getMessage(),
          null,
          "AuthenticationTypeException",
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR
      );

      String responseBody = objectMapper.writeValueAsString(errorResponse);
      response.getWriter().write(responseBody);
    }
  }
}
