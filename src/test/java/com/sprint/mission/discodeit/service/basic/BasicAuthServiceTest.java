package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.user.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.auth.AuthInvalidCredentialsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private BasicAuthService authService;

  @Test
  @DisplayName("login 성공: username/password 일치하면 UserDto 반환 + status(lastActiveAt) 갱신")
  void login_success_updatesStatusAndReturnsDto() {
    // given
    User user = new User("junyoung", "jun@test.com", "pw", null);
    UserStatus status = new UserStatus(user, Instant.EPOCH);
    user.attachStatus(status);

    LoginRequest req = new LoginRequest("junyoung", "pw");
    UserDto mapped = new UserDto(null, "junyoung", "jun@test.com", null, false);

    when(userRepository.findByUsername("junyoung")).thenReturn(Optional.of(user));
    when(userMapper.toDto(user)).thenReturn(mapped);

    Instant before = status.getLastActiveAt();

    // when
    UserDto result = authService.login(req);

    // then
    assertThat(result).isSameAs(mapped);
    assertThat(status.getLastActiveAt()).isAfter(before);

    verify(userRepository).findByUsername("junyoung");
    verify(userMapper).toDto(user);
    verifyNoMoreInteractions(userRepository, userMapper);
  }

  @Test
  @DisplayName("login 성공: status가 null이면 status 업데이트 없이도 정상 반환")
  void login_success_whenStatusNull_returnsDto() {
    // given
    User user = new User("junyoung", "jun@test.com", "pw", null);

    LoginRequest req = new LoginRequest("junyoung", "pw");
    UserDto mapped = new UserDto(null, "junyoung", "jun@test.com", null, false);

    when(userRepository.findByUsername("junyoung")).thenReturn(Optional.of(user));
    when(userMapper.toDto(user)).thenReturn(mapped);

    // when / then
    UserDto result = assertDoesNotThrow(() -> authService.login(req));

    // then
    assertThat(result).isSameAs(mapped);
    verify(userRepository).findByUsername("junyoung");
    verify(userMapper).toDto(user);
  }

  @Test
  @DisplayName("login 실패: username이 없으면 AuthInvalidCredentialsException")
  void login_fail_userNotFound() {
    // given
    LoginRequest req = new LoginRequest("nope", "pw");
    when(userRepository.findByUsername("nope")).thenReturn(Optional.empty());

    // when / then
    assertThrows(AuthInvalidCredentialsException.class, () -> authService.login(req));

    verify(userRepository).findByUsername("nope");
    verifyNoMoreInteractions(userRepository, userMapper);
  }

  @Test
  @DisplayName("login 실패: password 불일치면 AuthInvalidCredentialsException")
  void login_fail_passwordMismatch() {
    // given
    User user = new User("junyoung", "jun@test.com", "pw1234", null);
    LoginRequest req = new LoginRequest("junyoung", "pw12345");

    when(userRepository.findByUsername("junyoung")).thenReturn(Optional.of(user));

    // when / then
    assertThrows(AuthInvalidCredentialsException.class, () -> authService.login(req));

    verify(userRepository).findByUsername("junyoung");

    // 비번 불일치면 toDto 호출까지 못 감
    verifyNoMoreInteractions(userRepository, userMapper);
  }
}