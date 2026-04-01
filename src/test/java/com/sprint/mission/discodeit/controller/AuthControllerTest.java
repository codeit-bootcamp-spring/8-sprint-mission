package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.auth.controller.AuthController;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserMapper userMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private SessionRegistry sessionRegistry;

  // 기존 AuthService 기반 테스트를 UserDetailsService 구조로 전환하기 위한 mock
  @MockitoBean
  private DiscodeitUserDetailsService customUserDetailsService;

  @Test
  @DisplayName("CSRF 토큰 조회 성공 테스트")
  void getCsrfToken_Success() throws Exception {
    CsrfToken csrfToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "token-value");

    mockMvc.perform(get("/api/auth/csrf-token")
            .requestAttr(CsrfToken.class.getName(), csrfToken)
            .requestAttr("_csrf", csrfToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.token").value("token-value"))
        .andExpect(jsonPath("$.data.headerName").value("X-XSRF-TOKEN"))
        .andExpect(jsonPath("$.data.parameterName").value("_csrf"));
  }

  @Test
  @DisplayName("내 정보 조회 성공 테스트")
  void getMe_Success() throws Exception {
    UUID userId = UUID.randomUUID();
    User user = new User("testuser", "test@example.com", "encoded-password", null);
    UserDto userDto = new UserDto(userId, "testuser", "test@example.com", null, true, null);
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(user);
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );

    given(userMapper.toDto(user)).willReturn(userDto);

    SecurityContextHolder.getContext().setAuthentication(authentication);
    try {
      mockMvc.perform(get("/api/auth/me"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(userId.toString()))
          .andExpect(jsonPath("$.data.username").value("testuser"))
          .andExpect(jsonPath("$.data.email").value("test@example.com"))
          .andExpect(jsonPath("$.data.online").value(true));
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  @Test
  @DisplayName("내 정보 조회 실패 테스트 - 인증되지 않은 요청")
  void getMe_Failure_Unauthorized() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized());
  }
}