package com.sprint.mission.discodeit;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .requestMatchers(
                "/swagger-ui/**",
                "/api/v3/api-docs/**",
                "/swagger-ui.html"
            ).permitAll()
            .requestMatchers("/assets/**").permitAll()
            .requestMatchers("/", "/index.html", "/user-list.html").permitAll()
            .requestMatchers("/api/users/**", "/api/channels/**", "/api/messages/**",
                "/api/readStatuses/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/binaryContents/**").permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
