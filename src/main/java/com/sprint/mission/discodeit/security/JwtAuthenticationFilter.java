package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.store.JwtSessionRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
  private final DiscodeitUserDetailsService userDetailsService;
  private final ObjectMapper objectMapper;
  private final JwtSessionRegistry jwtSessionRegistry;


  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      String token = resolveToken(request);

      if (StringUtils.hasText(token)) {

        if (tokenProvider.validateAccessToken(token)) {

          String jti = tokenProvider.getTokenId(token);

          if (jwtSessionRegistry.isRevoked(jti)) {
            sendUnauthorized(response, "Token revoked");
            return;
          }

          String username = tokenProvider.getUsernameFromToken(token);
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);

          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());

          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
          sendUnauthorized(response, "Invalid JWT token");
          return;
        }
      }
    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      sendUnauthorized(response, "JWT authentication failed");
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


  private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {

    // 응답 헤더 설정
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // JSON 응답 전송
    String responseBody = objectMapper.createObjectNode().put("success", false)
        .put("message", message).toString();

    // 응답 바디 전송
    response.getWriter().write(responseBody);
  }
}


