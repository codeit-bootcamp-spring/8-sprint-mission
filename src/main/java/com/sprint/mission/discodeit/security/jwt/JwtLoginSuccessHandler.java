package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.JwtDTO;
import com.sprint.mission.discodeit.dto.JwtInformation;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import jakarta.servlet.ServletException;
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

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider tokenProvider;
  private final JwtRegistry jwtRegistry;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    // 응답 인코딩/콘텐츠 타입 설정
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    if (authentication.getPrincipal() instanceof DiscodeitUserDetails discodeitUserDetails) {
      try {
        String accessToken = tokenProvider.generateAccessToken(discodeitUserDetails);
        String refreshToken = tokenProvider.generateRefreshToken(discodeitUserDetails);

        tokenProvider.addRefreshCookie(response, refreshToken);

        UserDto userDto = discodeitUserDetails.getUserDto();

        JwtDTO jwtDto = new JwtDTO(userDto, accessToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

        jwtRegistry.registerJwtInformation(
            new JwtInformation(
                discodeitUserDetails.getUserDto(),
                accessToken,
                refreshToken
            )
        );
      } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(objectMapper.createObjectNode()
            .put("success", false)
            .put("message", "Token generation failed")
            .toString());
      }
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(objectMapper.createObjectNode()
          .put("success", false)
          .put("message", "Invalid principal")
          .toString());
    }
  }
}
