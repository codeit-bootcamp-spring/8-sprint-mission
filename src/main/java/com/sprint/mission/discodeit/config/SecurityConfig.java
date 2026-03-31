package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      LoginSuccessHandler loginSuccessHandler,
      LoginFailureHandler loginFailureHandler,
      SessionRegistry sessionRegistry) throws Exception {
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
            .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN))
        )
        // 세션 관리 고도화
        .sessionManagement(management -> management
            .sessionConcurrency(concurrency -> concurrency
                .maximumSessions(1)
                // 기존 세션 유지, 새 로그인 차단
                .maxSessionsPreventsLogin(true)
                // 저장소 지정
                .sessionRegistry(sessionRegistry)
            )
        )
        // 권한 설정
        .authorizeHttpRequests(
            auth -> auth
                .requestMatchers("/", "/index.html").permitAll()
                .requestMatchers("/api/auth/csrf-token", "/api/auth/login", "/api/auth/logout")
                .permitAll() // 인증 API
                .requestMatchers("/api/users").permitAll() // 회원가입
                .anyRequest().authenticated()
        );
    return http.build();
  }

  // ADMIN > CHANNEL_MANAGER > USER
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("ADMIN").implies("CHANNEL_MANAGER")
        .role("CHANNEL_MANAGER").implies("USER")
        .build();
  }

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  // 세션 추적용 저장소 빈
  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring()
        .requestMatchers("/assets/**", "/favicon.ico", "/static/**", "/swagger-ui/**",
            "/v3/api-docs/**", "/actuator/**");
  }
}