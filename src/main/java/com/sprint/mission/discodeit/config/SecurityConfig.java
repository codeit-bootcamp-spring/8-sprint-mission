package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.handler.CustomAccessDeniedHandler;
import com.sprint.mission.discodeit.handler.CustomAuthenticationEntryPoint;
import com.sprint.mission.discodeit.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.handler.LoginSuccessHandler;
import com.sprint.mission.discodeit.handler.SpaCsrfTokenRequestHandler;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${spring.security.remember-me.key}")
  private String rememberMeKey;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      LoginSuccessHandler loginSuccessHandler,
      LoginFailureHandler loginFailureHandler,
      CustomAuthenticationEntryPoint authenticationEntryPoint,
      CustomAccessDeniedHandler accessDeniedHandler,
      SessionRegistry sessionRegistry,
      RememberMeServices rememberMeServices) throws Exception {

    http
        .headers(headers -> headers
            .frameOptions(FrameOptionsConfig::sameOrigin)
        )
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            .ignoringRequestMatchers("/h2-console/**")
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers("/api/auth/csrf-token").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
            .requestMatchers("/api/auth/login").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(login -> login
            .loginProcessingUrl("/api/auth/login")
            .successHandler(loginSuccessHandler)
            .failureHandler(loginFailureHandler)
            .permitAll()
        )
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
        )
        .sessionManagement(management -> management
            .sessionConcurrency(concurrency -> concurrency
                .maximumSessions(1)
                .sessionRegistry(sessionRegistry)
            )
        )
        .rememberMe(remember -> remember
            .rememberMeServices(rememberMeServices)
            .key(rememberMeKey)
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        );

    return http.build();
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public JdbcTokenRepositoryImpl tokenRepository(DataSource dataSource) {
    JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
    tokenRepository.setDataSource(dataSource);

    return tokenRepository;
  }

  @Bean
  public PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices(
      UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository
  ) {
    PersistentTokenBasedRememberMeServices rememberMeServices =
        new PersistentTokenBasedRememberMeServices(
            rememberMeKey,
            userDetailsService,
            tokenRepository
        );

    rememberMeServices.setTokenValiditySeconds(60 * 60 * 24 * 7);
    rememberMeServices.setCookieName("remember-me");
    rememberMeServices.setParameter("remember-me");

    return rememberMeServices;
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.ignoring()
        .requestMatchers("/favicon.ico", "/error", "/index.html")
        .requestMatchers("/static/**", "/assets/**", "/css/**", "/js/**", "/images/**");
  }

  @Bean
  public RoleHierarchy roleHierarchy() {

    return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_CHANNEL_MANAGER > ROLE_USER");
  }

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }
}
