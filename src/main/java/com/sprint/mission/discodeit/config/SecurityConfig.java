package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      LoginSuccessHandler loginSuccessHandler,
      LoginFailureHandler loginFailureHandler) throws Exception {
    http
        // CSRF 설정 - SPA 환경에 맞게 쿠키 기반 저장소, 커스텀 핸들러 사용
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        )
        // formLogin 활성화, 경로 설정
        .formLogin(login -> login
            .loginProcessingUrl("/api/auth/login")
            .successHandler(loginSuccessHandler)
            .failureHandler(loginFailureHandler)
        )
        // 로그아웃 설정
        .logout(logout -> logout
            // 로그아웃 URL 설정
            .logoutUrl("/api/auth/logout")
            // 성공 시 리다이렉트 대신 204 응답 반환으로 대체
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            // 추가 보안 (세션 무효화 및 쿠키 삭제 (기본값이지만 명시))
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        )
        // 인증되지 않은 접근 시 리다이렉트 X, 401 에러 반환
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        )
        // 권한 설정
        .authorizeHttpRequests(
            auth -> auth
                .requestMatchers("/", "/index.html").permitAll()
                .requestMatchers("/api/auth/login", "/api/users", "/api/auth/csrf-token")
                .permitAll()
                .anyRequest().authenticated()
        );
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring()
        .requestMatchers("/assets/**", "/favicon.ico", "/static/**", "/swagger-ui/**",
            "/v3/api-docs/**");
  }
}