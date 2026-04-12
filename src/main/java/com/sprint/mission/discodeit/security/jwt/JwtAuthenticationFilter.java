package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.service.basic.DiscodeitUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider tokenProvider;
  private final DiscodeitUserDetailsService discodeitUserDetailsService;
  private final JwtRegistry jwtRegistry;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    try {
      // Authorization 헤더에서 Bearer 토큰을 추출
      String token = resolveToken(request);

      // 토큰이 존재하는지 확인
      if (StringUtils.hasText(token)) {
        if (tokenProvider.validateAccessToken(token)
            && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {

          String username = tokenProvider.getUsernameFromToken(token);
          UserDetails userDetails = discodeitUserDetailsService.loadUserByUsername(username);

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails,
                  null,
                  userDetails.getAuthorities()
              );

          // 인증 객체에 현재 요청 정보를 추가한다.
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          // 인증 객체를 SecurityContext에 저장한다.
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          // 토큰 유효성 검사 실패 시 처리(401)
          sendUnauthorized(response, ErrorCode.INVALID_TOKEN);
          return;
        }
      }
    } catch (Exception e) {
      // 인증 과정에서 예외 발생 시 인증 컨텍스트를 초기화하고 401 응답을 반환한다.
      SecurityContextHolder.clearContext();
      sendUnauthorized(response, ErrorCode.UNEXPECTED_PRINCIPAL_TYPE);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private void sendUnauthorized(HttpServletResponse response, ErrorCode errorCode)
      throws IOException {

    // 응답 헤더 설정
    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // JSON 응답 전송
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        errorCode.name(),
        errorCode.getMessage(),
        null,
        "AuthenticationTypeException",
        errorCode.getStatus().value()
    );

    // 응답 바디 전송
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
