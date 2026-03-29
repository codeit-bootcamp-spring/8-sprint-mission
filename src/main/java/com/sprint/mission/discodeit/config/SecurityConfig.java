package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 설정: 쿠키 기반 CSRF 토큰 사용
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                )
                // HTTP 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers(
                                "/api/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/channels").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/channels/*").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/channels/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/channels/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/messages").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/messages").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/messages/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/messages/*").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                );
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
                    .resolveCsrfTokenValue(request, csrfToken)
                    ;
        }
    }
}
