package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserAlreadyExistException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // Mockito를 사용하기 위한 확장
class UserServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserStatusRepository userStatusRepository;
  @Mock
  private UserMapper userMapper;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private BinaryContentStorage binaryContentStorage;

  @InjectMocks
  private BasicUserService userService;

  @Nested
  @DisplayName("사용자 생성 테스트")
  class CreateUser {

    @Test
    @DisplayName("성공: 유효한 정보로 사용자를 생성")
    void create_success() {
      //given
      UserCreateRequest request = new UserCreateRequest("testUser", "test@email.com",
          "password123");
      User user = new User(request.username(), request.email(), request.password(), null);
      UserDto expectedDto = new UserDto(
          UUID.randomUUID(),
          request.username(),
          request.email(),
          null,  // profile (BinaryContentDto)
          false  // online (Boolean)
      );

      given(userRepository.existsByEmail(request.email())).willReturn(false);
      given(userRepository.existsByUsername(request.username())).willReturn(false);
      given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

      //when
      UserDto result = userService.create(request, Optional.empty());

      //then
      assertThat(result).isEqualTo(expectedDto);
      then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("실패: 중복된 이메일이 존재하면 예외가 발생")
    void create_fail_duplicateEmail() {
      //given
      UserCreateRequest request = new UserCreateRequest("testUser", "duplicate@email.com",
          "password123");
      given(userRepository.existsByEmail(request.email())).willReturn(true);

      //when & then
      assertThatThrownBy(() -> userService.create(request, Optional.empty()))
          .isInstanceOf(UserAlreadyExistException.class);
    }
  }

  @Nested
  @DisplayName("사용자 수정 테스트")
  class UpdateUser {

    @Test
    @DisplayName("성공: 정보를 정상적으로 수정")
    void update_success() {
      //given
      UUID userId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("newUsername", "new@email.com",
          "newPassword");
      User user = new User("old", "old@email.com", "oldPass", null);

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(userRepository.existsByEmail(request.newEmail())).willReturn(false);
      given(userRepository.existsByUsername(request.newUsername())).willReturn(false);

      //when
      userService.update(userId, request, Optional.empty());

      //then
      assertThat(user.getUsername()).isEqualTo(request.newUsername());
      assertThat(user.getEmail()).isEqualTo(request.newEmail());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자 ID면 예외가 발생")
    void update_fail_notFound() {
      //given
      UUID userId = UUID.randomUUID();
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      //when & then
      assertThatThrownBy(
          () -> userService.update(userId, mock(UserUpdateRequest.class), Optional.empty()))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("사용자 삭제 테스트")
  class DeleteUser {

    @Test
    @DisplayName("성공: 사용자를 삭제")
    void delete_success() {
      //given
      UUID userId = UUID.randomUUID();
      given(userRepository.existsById(userId)).willReturn(true);

      //when
      userService.delete(userId);

      //then
      then(userRepository).should().deleteById(userId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자 삭제 시 예외가 발생")
    void delete_fail_notFound() {
      //given
      UUID userId = UUID.randomUUID();
      given(userRepository.existsById(userId)).willReturn(false);

      //when & then
      assertThatThrownBy(() -> userService.delete(userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }
}