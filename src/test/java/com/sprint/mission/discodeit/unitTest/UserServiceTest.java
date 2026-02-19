package com.sprint.mission.discodeit.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserException.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.UserException.DuplicateUsernameException;
import com.sprint.mission.discodeit.exception.UserException.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private BasicUserService userService;

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserStatusRepository userStatusRepository;
  @Mock
  private UserMapper userMapper;
  @Mock
  private PasswordEncoder passwordEncoder;

  private UserCreateRequest createRequest;
  private UserUpdateRequest updateRequest;
  private User user;
  private UserDto userDto;

  @BeforeEach
  void setUp() {
    createRequest = new UserCreateRequest("testUser", "test@example.com", "password123");
    updateRequest = new UserUpdateRequest("testUser2", "test2@example.com", "password123");
    user = new User("testUser", "test@example.com", "encodedPassword", null);
    // ID가 없으면 서비스 로직 중 NPE 발생 가능성이 있으므로 ID 주입
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    userDto = new UserDto(user.getId(), "testUser", "test@example.com", null, false);
  }

  @Test
  @DisplayName("유저 생성 테스트 - 성공")
  void create_user_success() {
    // given
    given(userRepository.existsByUsername(createRequest.username())).willReturn(false);
    given(userRepository.existsByEmail(createRequest.email())).willReturn(false);
    given(passwordEncoder.encode(createRequest.password())).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(any(User.class))).willReturn(userDto);

    // when
    UserDto result = userService.create(createRequest, Optional.empty());

    // then
    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo(createRequest.username());
    assertThat(result.email()).isEqualTo(createRequest.email());

    then(userRepository).should().save(any(User.class));
    then(userStatusRepository).should().save(any());
  }

  @Test
  @DisplayName("유저 생성 테스트 - 실패(중복된 사용자명)")
  void create_user_fail_duplicate_username() {
    // given
    given(userRepository.existsByUsername(createRequest.username())).willReturn(true);

    // when
    assertThatThrownBy(() -> userService.create(createRequest, Optional.empty()))
        .isInstanceOf(DuplicateUsernameException.class);

    // then
    then(userRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("유저 생성 테스트 - 실패(중복된 이메일)")
  void create_user_fail_duplicate_email() {
    // given
    given(userRepository.existsByUsername(createRequest.username())).willReturn(false);
    given(userRepository.existsByEmail(createRequest.email())).willReturn(true);

    // when
    assertThatThrownBy(() -> userService.create(createRequest, Optional.empty()))
        .isInstanceOf(DuplicateEmailException.class);

    // then
    then(userRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("유저 수정 테스트 - 성공")
  void update_user_success() {
    //given
    UUID userId = user.getId();

    String newUsername = updateRequest.newUsername();
    String newEmail = updateRequest.newEmail();
    String newPassword = updateRequest.newPassword();

    given(userRepository.findById(userId)).willReturn(Optional.ofNullable(user));
    given(userRepository.existsByUsername(newUsername)).willReturn(false);
    given(userRepository.existsByEmail(newEmail)).willReturn(false);
    given(passwordEncoder.encode(newPassword)).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(any(User.class))).willAnswer(invocation -> {
      User user = invocation.getArgument(0);
      return new UserDto(user.getId(), user.getUsername(), user.getEmail(), null, false);
    });

    //when
    UserDto result = userService.update(userId, updateRequest, Optional.empty());

    //then
    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo(newUsername);
    assertThat(result.email()).isEqualTo(newEmail);

    then(userRepository).should().save(any(User.class));
  }

  @Test
  @DisplayName("유저 수정 테스트 - 실패(존재하지 않는 유저)")
  void update_user_fail_not_found() {
    //given
    UUID userId = user.getId();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    //when
    assertThatThrownBy(() -> userService.update(userId, updateRequest, Optional.empty()))
        .isInstanceOf(UserNotFoundException.class);

    //then
    then(userRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("유저 수정 테스트 - 실패(중복된 사용자명)")
  void update_user_fail_duplicate_username() {
    //given
    UUID userId = user.getId();
    String newUsername = updateRequest.newUsername();

    given(userRepository.findById(userId)).willReturn(Optional.ofNullable(user));
    given(userRepository.existsByUsername(newUsername)).willReturn(true);

    //when
    assertThatThrownBy(() -> userService.update(userId, updateRequest, Optional.empty()))
        .isInstanceOf(DuplicateUsernameException.class);

    //then
    then(userRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("유저 수정 테스트 - 실패(중복된 이메일)")
  void update_user_fail_duplicate_email() {
    //given
    UUID userId = user.getId();
    String newUsername = updateRequest.newUsername();
    String newEmail = updateRequest.newEmail();

    given(userRepository.findById(userId)).willReturn(Optional.ofNullable(user));
    given(userRepository.existsByUsername(newUsername)).willReturn(false);
    given(userRepository.existsByEmail(newEmail)).willReturn(true);

    //when
    assertThatThrownBy(() -> userService.update(userId, updateRequest, Optional.empty()))
        .isInstanceOf(DuplicateEmailException.class);

    //then
    then(userRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("유저 삭제 테스트 - 성공")
  void delete_user_success() {
    //given
    UUID userId = user.getId();

    given(userRepository.findById(userId)).willReturn(Optional.ofNullable(user));
    //when
    userService.delete(userId);
    //then
    then(userRepository).should().delete(user);
  }

  @Test
  @DisplayName("유저 삭제 테스트 - 실패(존재하지 않는 유저)")
  void delete_user_fail_not_found() {
    //given
    UUID userId = user.getId();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    //when
    assertThatThrownBy(() -> userService.delete(userId))
        .isInstanceOf(UserNotFoundException.class);

    //then
    then(userRepository).should(never()).delete(any());
  }
}
