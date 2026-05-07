package com.sprint.mission.discodeit.global.config;

import com.sprint.mission.discodeit.auth.handler.CustomAccessDeniedHandler;
import com.sprint.mission.discodeit.auth.handler.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.auth.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.auth.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.auth.handler.SpaCsrfTokenRequestHandler;
import com.sprint.mission.discodeit.global.secutiry.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.global.secutiry.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.global.secutiry.JwtRegistry;
import com.sprint.mission.discodeit.global.secutiry.JwtTokenProvider;
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig
{
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 디버깅
    @Bean
    public CommandLineRunner debugFilterChain(SecurityFilterChain filterChain) {
        return args -> {
            int filterSize = filterChain.getFilters().size();

            List<String> filterNames = IntStream.range(0, filterSize)
                    .mapToObj(idx -> String.format("\t[%s/%s] %s", idx + 1, filterSize,
                            filterChain.getFilters().get(idx).getClass()))
                    .toList();

            System.out.println("현재 적용된 필터 체인 목록:");
            filterNames.forEach(System.out::println);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomAccessDeniedHandler customAccessDeniedHandler,
                                           JwtLoginSuccessHandler jwtLoginSuccessHandler,
                                           JwtLogoutHandler jwtLogoutHandler,
                                           LoginFailureHandler loginFailureHandler,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception
    {
        http
            // 1. CSRF 설정
            .csrf(csrf -> csrf
                    // 쿠키 기반 CSRF 토큰 저장소
                    // JS에서 쿠키를 읽을 수 있도록 HttpOnly=false
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    // SPA 환경에 적합한 SpaCsrfTokenRequestHandler로 교체
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                    .ignoringRequestMatchers("/ws/**")
            )
            // 2. HTTP 요청 권한 설정
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/", "/index.html", "/favicon.ico", "/assets/**",
                            "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**"
                    ).permitAll()
                    .requestMatchers("/api/auth/csrf-token").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // 회원가입
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                    .requestMatchers("/api/auth/logout").permitAll()

                    // Public 채널 관리
                    .requestMatchers(HttpMethod.POST, "/api/channels/public").hasRole("CHANNEL_MANAGER")
                    .requestMatchers(HttpMethod.PATCH, "/api/channels/*").hasRole("CHANNEL_MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/channels/*").hasRole("CHANNEL_MANAGER")

                    // 사용자 권한 변경
                    .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            // 3. 세션 관리 설정
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 4. Form 기반 로그인 설정
            .formLogin(login -> login
                    .loginProcessingUrl("/api/auth/login")
                    .successHandler(jwtLoginSuccessHandler)
                    .failureHandler(loginFailureHandler)
            )
            // 5. 로그아웃 설정
            .logout(logout -> logout
                    .logoutUrl("/api/auth/logout")
                    .addLogoutHandler(jwtLogoutHandler)
                    .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            )
            // 6. 예외 처리 설정
            .exceptionHandling(ex -> ex
                    // 401 인증 실패(나중에 수정)
                    .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                    // 403 권한 없음
                    .accessDeniedHandler(customAccessDeniedHandler)
            )
            // 7. jwt 토큰 삽입
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 권한 계층 구조
    // 관리자(매니저, 일반 포함), 매니저(일반 포함)
    @Bean
    public RoleHierarchy roleHierarchy() {

        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_CHANNEL_MANAGER > ROLE_USER");
    }

    // Method Security에서 RoleHierarchy를 사용할 수 있도록 설정
    @Bean
    public static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();

        handler.setRoleHierarchy(roleHierarchy);

        return handler;
    }

    @Bean
    public JwtRegistry jwtRegistry(JwtTokenProvider jwtTokenProvider) {
        return new InMemoryJwtRegistry(1, jwtTokenProvider);
    }
}
