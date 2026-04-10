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
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {

    ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        errorCode.name(),
        errorCode.getMessage(),
        null,
        accessDeniedException.getClass().getSimpleName(),
        HttpServletResponse.SC_FORBIDDEN
    );

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
