package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.auth.controller.AuthController;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.security.store.JwtSessionRegistry;
import com.sprint.mission.discodeit.security.store.JwtTokenEntity;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.Cookie;
import java.time.OffsetDateTime;
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

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private JwtSessionRegistry jwtSessionRegistry;

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
  @DisplayName("리프레시 토큰 Rotation으로 access token 재발급 성공")
  void refreshAccessToken_Success_WithRotation() throws Exception {
    String refreshToken = "old-refresh-token";
    String rotatedRefreshToken = "new-refresh-token";
    String username = "refreshuser";
    String accessToken = "new-access-token";
    String oldRefreshJti = "old-jti";
    String newRefreshJti = "new-jti";

    User user = new User(username, "refresh@example.com", "encoded-password", null);
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(user);
    UserDto userDto = new UserDto(UUID.randomUUID(), username, "refresh@example.com", null, true, null);
    JwtTokenEntity rotatedEntity = new JwtTokenEntity(newRefreshJti, username, "refresh",
        OffsetDateTime.now(), OffsetDateTime.now().plusDays(7));

    given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(true);
    given(jwtTokenProvider.getTokenId(refreshToken)).willReturn(oldRefreshJti);
    given(jwtSessionRegistry.isRevoked(oldRefreshJti)).willReturn(false);
    given(jwtTokenProvider.getUsernameFromToken(refreshToken)).willReturn(username);
    given(customUserDetailsService.loadUserByUsername(username)).willReturn(userDetails);
    given(jwtTokenProvider.generateAccessToken(userDetails)).willReturn(accessToken);
    given(jwtTokenProvider.generateRefreshToken(userDetails)).willReturn(rotatedRefreshToken);
    given(jwtTokenProvider.getTokenId(rotatedRefreshToken)).willReturn(newRefreshJti);
    given(jwtTokenProvider.toEntity(rotatedRefreshToken)).willReturn(rotatedEntity);
    given(userMapper.toDto(user)).willReturn(userDto);

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userDto.username").value(username))
        .andExpect(jsonPath("$.accessToken").value(accessToken));

    verify(jwtTokenProvider).addRefreshCookie(org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.eq(rotatedRefreshToken));
  }

  @Test
  @DisplayName("리프레시 토큰이 없으면 401 응답")
  void refreshAccessToken_Failure_WhenCookieMissing() throws Exception {
    mockMvc.perform(post("/api/auth/refresh"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_USER_CREDENTIALS"));
  }

  @Test
  @DisplayName("폐기된 리프레시 토큰이면 401 응답")
  void refreshAccessToken_Failure_WhenRevokedToken() throws Exception {
    String refreshToken = "revoked-refresh-token";
    String oldRefreshJti = "revoked-jti";

    given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(true);
    given(jwtTokenProvider.getTokenId(refreshToken)).willReturn(oldRefreshJti);
    given(jwtSessionRegistry.isRevoked(oldRefreshJti)).willReturn(true);

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_USER_CREDENTIALS"));
  }
}