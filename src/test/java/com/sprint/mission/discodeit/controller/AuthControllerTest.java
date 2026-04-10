package com.sprint.mission.discodeit.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    if (!userRepository.existsByUsername("admin")) {
      userRepository.save(new User(
          "admin",
          "admin@example.com",
          passwordEncoder.encode("admin1234"),
          null,
          Role.ADMIN
      ));
    }
  }

  @Test
  @DisplayName("CSRF 토큰 발급 테스트 - 204 No Content 확인")
  void getCsrfToken_Success() throws Exception {

    mockMvc.perform(get("/api/auth/csrf-token"))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("로그인 요청 테스트 - 실제 시큐리티 필터 체인 검증")
  void login_Success() throws Exception {

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", "admin")
            .param("password", "admin1234")
            .param("remember-me", "true")
            .with(csrf())) // CSRF 필수
        .andExpect(status().isOk());
  }
}