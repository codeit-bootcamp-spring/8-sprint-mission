package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.handler.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.session.SessionInformation;
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
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           SessionRegistry sessionRegistry,
                                           LoginSuccessHandler loginSuccessHandler,
                                           LoginFailureHandler loginFailureHandler,
                                           CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                                           CustomAccessDeniedHandler customAccessDeniedHandler,
                                           DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
                // CSRF 설정: 쿠키 기반 CSRF 토큰 사용
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                )
                // HTTP 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html").permitAll()
                        .requestMatchers(
                                "/api/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/auth/csrf-token").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/channels").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/channels/public").hasRole("CHANNEL_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/channels/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/channels/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/messages").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/messages").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/messages/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/messages/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                // 세션 관리 설정
                // 동일한 계정으로 동시 로그인할 수 없도록 설정
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation
                                .migrateSession()
                        )
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)
                                .maxSessionsPreventsLogin(false)
                                .sessionRegistry(sessionRegistry)
                                .expiredSessionStrategy(new CustomSessionExpiredStrategy())
                        )
                )
                // form 기반 로그인 활성화
                .formLogin(form -> form
                        // 로그인을 처리하는 URL 정의
                        .loginProcessingUrl("/api/auth/login")
                        // 로그인 성공 시 처리할 핸들러 정의
                        .successHandler(loginSuccessHandler)
                        // 로그인 실패 시 처리할 핸들러 정의
                        .failureHandler(loginFailureHandler)
                        // 로그인 페이지는 인증 없이 모두 접근 가능해야 한다.
                        .permitAll()
                )
                // Http Basic Authentication(기본 인증) 비활성화 (보안상 위험함)
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout
                        // 로그아웃을 처리하는 URL 정의
                        .logoutUrl("/api/auth/logout")
                        // 로그아웃 성공 시 처리할 핸들러 정의
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                        // 로그아웃 페이지를 인증 없이 모두 접근 가능해야 함
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                // 인증 프로파이더 설정 (앞서 bean으로 정의된 DaoAuthenticationProvider 사용)
                .authenticationProvider(authenticationProvider);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner debugFilterChain(SecurityFilterChain filterChain) {
        return args -> {
            int filterSize = filterChain.getFilters().size();

            List<String> filterNames = IntStream.range(0, filterSize)
                    .mapToObj(idx -> String.format("\t[%s/%s] %s", idx + 1, filterSize,
                            filterChain.getFilters().get(idx).getClass()))
                    .toList();
            log.info("현재 적용된 필터 체인 목록: ");
            filterNames.forEach(log::info);
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                // 브라우저 기본 요청 및 에러 페이지
                .requestMatchers("/favicon.ico", "/error")
                // 정적 리소스
                .requestMatchers("/static/**", "/css/**", "/js/**");
    }

    public static class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(
                HttpServletRequest request,
                HttpServletResponse response,
                Supplier<CsrfToken> csrfToken
        ) {
            this.xor.handle(request, response, csrfToken);
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String headerValue = request.getHeader(csrfToken.getHeaderName());
            return (org.springframework.util.StringUtils.hasText(headerValue) ? this.plain : this.xor)
                    .resolveCsrfTokenValue(request, csrfToken);
        }
    }

    // 권한 계층 구조를 정의
    // ADMIN > CHANNEL_MANAGER > USER 구조로 설정하여 ADMIN 권한이 CHANNEL_MANAGER, USER 권한을 포함
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchy hierarchy = RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_CHANNEL_MANAGER > ROLE_USER");

        log.info("[SecurityConfig] RoleHierarchy 설정 완료");

        return hierarchy;
    }

    // Method Security에서 RoleHierarchy을 사용할 수 있도록 설정
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy hierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();

        handler.setRoleHierarchy(hierarchy);

        log.info("[SecurityConfig] MethodSecurityExpressionHandler 설정 완료!");

        return handler;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // Remember-Me 기능을 위한 JdbcTokenRepository Bean 설정
    // Remember-Me 토큰을 쿠키가 아닌 데이터베이스에 저장하여 Remember-Me 기능을 구현한다.
    @Bean
    public JdbcTokenRepositoryImpl tokenRepository(DataSource dataSource) {
        log.info("[SecurityConfig] JdbcTokenRepository 생성...");
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();

        tokenRepository.setDataSource(dataSource);

        log.info("[SecurityConfig] JdbcTokenRepository 설정 완료...");
        return tokenRepository;
    }

    // Remember-Me 서비스 Bean 설정
    // 테스트를 위해 1분으로 설정
    @Bean
    public PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices(
            UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository
    ) {
        PersistentTokenBasedRememberMeServices rememberMeServices =
                new PersistentTokenBasedRememberMeServices(
                        "discodeit-key",
                        userDetailsService,
                        tokenRepository
                );

        rememberMeServices.setTokenValiditySeconds(60);
        rememberMeServices.setCookieName("remember-me");
        rememberMeServices.setParameter("remember-me");

        log.info("[SecurityConfig] Remember-Me 설정 완료...");

        return rememberMeServices;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
