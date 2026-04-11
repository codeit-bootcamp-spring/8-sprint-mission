package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 사용자가 보낸 요청 헤더에 토큰이 있는지, 있다면 진짜인지 검사하는 필터
 * OncePerRequestFilter를 상속하여 사용자의 한 번의 요청당 딱 한번만 실행되도록
 * 보장해준다. (인증 로직은 리소스(DB 조회, JWT 검증)를 많이 소모하는 작업이기 때문이다.)
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // HTTP 요청 헤더에서 "Authorization" 부분 추출
    String bearerToken = request.getHeader("Authorization");

    // 토큰이 존재하는지 && "Bearer "로 시작하는지 확인
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      // "Bearer " 문자열(공백 포함 7자)을 제외한 실제 토큰 값만 추출
      String token = bearerToken.substring(7);

      // 토큰 서명 일치하는지, 만료 여부 확인
      if (jwtTokenProvider.validateToken(token)) {

        // 토큰 내부의 Subject 추출
        String userIdStr = jwtTokenProvider.extractSubject(token);

        UserDetails userDetails = userDetailsService.loadUserById(UUID.fromString(userIdStr));

        // 시큐리티가 이해할 수 있는 형태의 인증 토큰 생성
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // 시큐리티 컨텍스트 홀더에 인증 객체 넣는다.
        // 이후 컨트롤러에서 @AuthenticationPrincipal로 인증 객체 꺼내 쓸 수 있다.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 다음 필터로 요청을 넘긴다.
        filterChain.doFilter(request, response);
      }
    }

  }
}
