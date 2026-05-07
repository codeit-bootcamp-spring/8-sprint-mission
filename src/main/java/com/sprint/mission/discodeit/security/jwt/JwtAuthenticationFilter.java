package com.sprint.mission.discodeit.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final JwtRegistry jwtRegistry;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    // 헤더에 Bearer 토큰이 없으면 다음 필터로 넘기기 (인증X)
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // 'Bearer ' 뒤의 실제 토큰 추출
    String token = authHeader.substring(7);

    // 토큰 유효성 검증
    if (jwtTokenProvider.validateToken(token)
        && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
      // 유효하면 username 추출해서 유저 정보 DB 조회
      String username = jwtTokenProvider.extractUsername(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      // Spring Security 용 인증 객체 생성
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      // SecurityContext에 인증 객체 주입 (로그인 된 유저임을 알려줌)
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    // 필터체인 진행
    filterChain.doFilter(request, response);
  }
}
