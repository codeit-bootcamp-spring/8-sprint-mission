package com.sprint.mission.discodeit.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.auth.constants.AuthConstants;
import com.sprint.mission.discodeit.auth.dto.AuthErrorResponse;
import com.sprint.mission.discodeit.auth.handler.CustomAccessDeniedHandler;
import com.sprint.mission.discodeit.auth.handler.CustomSessionExpiredStrategy;
import com.sprint.mission.discodeit.auth.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.auth.handler.LoginSuccessHandler;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

  private final SpaCsrfTokenRequestHandler spaCsrfTokenRequestHandler;
  private final CustomSessionExpiredStrategy customSessionExpiredStrategy;
  private final ObjectMapper objectMapper;
  private final String[] PUBLIC_URLS = {"/", "/index.html", "/favicon.ico", "/assets/**", "/error",
      "/swagger-ui/**", "/v3/api-docs/**", "/api/auth/login", "/api/auth/logout"};

  @Value("${app.security.remember-me.key}")
  private String rememberMeKey;

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  /**
   * HttpSession 만료 시 이벤트를 통해 SessionRegistry의 SessionInformation도 자동으로 만료 처리한다.
   */
  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      LoginSuccessHandler loginSuccessHandler, LoginFailureHandler loginFailureHandler,
      CustomAccessDeniedHandler customAccessDeniedHandler,
      DaoAuthenticationProvider authenticationProvider,
      PersistentTokenBasedRememberMeServices rememberMeServices) throws Exception {
    http
        // 접근 권한 설정
        .authorizeHttpRequests(
            auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // 회원가입
                .anyRequest().authenticated()
        )
        // CSRF 설정
        .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
            // 로그인, 로그아웃, 회원가입은 CSRF 검증에서 제외
            .ignoringRequestMatchers("/api/auth/login", "/api/auth/logout", "/api/users"))
        // 폼 로그인 설정
        .formLogin(
            formLogin -> formLogin.loginPage("/index.html").loginProcessingUrl("/api/auth/login")
                .successHandler(loginSuccessHandler).failureHandler(loginFailureHandler)
                .permitAll())
        // 로그아웃 설정
        .logout(logout -> logout.logoutUrl("/api/auth/logout")
            .deleteCookies(AuthConstants.COOKIE_SESSION_ID, AuthConstants.COOKIE_XSRF_TOKEN)
            .invalidateHttpSession(true).clearAuthentication(true)
            .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200)))
        // 예외 처리 설정
        .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
          AuthErrorResponse errorResponse = new AuthErrorResponse(AuthConstants.ERROR_UNAUTHORIZED,
              AuthConstants.MSG_LOGIN_REQUIRED);

          response.setStatus(401);
          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
          response.setCharacterEncoding("UTF-8");
          response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }).accessDeniedHandler(customAccessDeniedHandler))
        // 동시 로그인 제한 및 세션 설정
        .sessionManagement(management -> management.sessionConcurrency(
            concurrency -> concurrency.maximumSessions(1)
                .expiredSessionStrategy(customSessionExpiredStrategy)
                .sessionRegistry(sessionRegistry()))).rememberMe(
            rememberMe -> rememberMe.key(rememberMeKey).tokenValiditySeconds(7 * 24 * 60 * 60) // 7일
                .rememberMeServices(rememberMeServices))
        // 인증 제공자
        .authenticationProvider(authenticationProvider);

    return http.build();
  }

  @Bean
  public JdbcTokenRepositoryImpl tokenRepository(DataSource dataSource) {

    log.info("[SecurityConfig] JdbcTokenRepository 생성");

    JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
    tokenRepository.setDataSource(dataSource);

    log.info("[SecurityConfig] JdbcTokenRepository 설정 완료");
    return tokenRepository;
  }

  @Bean
  public PersistentTokenBasedRememberMeServices rememberMeServices(
      UserDetailsService userDetailsService, JdbcTokenRepositoryImpl tokenRepository) {

    // 토큰을 데이터베이스에 저장하는 방법으로 설정
    PersistentTokenBasedRememberMeServices rememberMeServices = new PersistentTokenBasedRememberMeServices(
        rememberMeKey, userDetailsService, tokenRepository);

    // Remember-Me 토큰의 유효 기간을 테스트 용도로 60초 설정 (운영시에는 대개 1주 이상)
    rememberMeServices.setTokenValiditySeconds(60);
    rememberMeServices.setCookieName("remember-me");
    rememberMeServices.setParameter("remember-me");

    log.info("[SecurityConfig] Remember-Me 설정 완료!");

    return rememberMeServices;
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(bCryptPasswordEncoder());
    return authProvider;
  }

  /**
   * 권한 계층 구조: ADMIN > CHANNEL_MANAGER > USER
   * ADMIN 은 CHANNEL_MANAGER, USER 의 모든 권한을 포함한다.
   * CHANNEL_MANAGER 는 USER 의 모든 권한을 포함한다.
   */
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_CHANNEL_MANAGER > ROLE_USER");
  }

  /**
   * 메서드 보안 표현식 핸들러에 RoleHierarchy 를 등록한다.
   * static @Bean 으로 선언해야 BeanPostProcessor 초기화 단계의 순환 의존성을 방지할 수 있다.
   */
  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();

    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }
}
