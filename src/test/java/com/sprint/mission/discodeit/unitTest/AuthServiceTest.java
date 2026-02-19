package com.sprint.mission.discodeit.unitTest;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.AuthException.AuthUserNotFound;
import com.sprint.mission.discodeit.exception.AuthException.InvalidPassword;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @InjectMocks
  private BasicAuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserMapper userMapper;

  private User user;
  private LoginRequest loginRequest;

  @BeforeEach
  void setUp() {
    user = new User("testUser", "test@example.com", "encodedPassword", null);
    loginRequest = new LoginRequest("testUser", "password123");
  }

  @Test
  @DisplayName("로그인 성공")
  void login_success() {
    // given
    UserDto userDto = new UserDto(UUID.randomUUID(), "testUser", "test@example.com", null, true);

    given(userRepository.findByUsername(loginRequest.username()))
        .willReturn(Optional.of(user));
    given(passwordEncoder.matches(loginRequest.password(), user.getPassword()))
        .willReturn(true);
    given(userMapper.toDto(user))
        .willReturn(userDto);

    // when
    UserDto result = authService.login(loginRequest);

    // then
    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo(loginRequest.username());
  }

  @Test
  @DisplayName("로그인 실패 - 존재하지 않는 사용자")
  void login_fail_userNotFound() {
    // given
    given(userRepository.findByUsername(loginRequest.username()))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(AuthUserNotFound.class);
  }

  @Test
  @DisplayName("로그인 실패 - 잘못된 비밀번호")
  void login_fail_invalidPassword() {
    // given
    given(userRepository.findByUsername(loginRequest.username()))
        .willReturn(Optional.of(user));
    given(passwordEncoder.matches(loginRequest.password(), user.getPassword()))
        .willReturn(false);

    // when & then
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(InvalidPassword.class);
  }
}
