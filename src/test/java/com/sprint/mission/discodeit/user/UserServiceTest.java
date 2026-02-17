package com.sprint.mission.discodeit.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
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
  private BasicUserService sut;

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String USERNAME = "testuser";
  private static final String EMAIL = "test@example.com";
  private static final String PASSWORD = "password";

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("성공: 사용자 생성 후 UserDto 반환")
    void create_success() {
      // given
      UserCreateRequest request = new UserCreateRequest(USERNAME, EMAIL, PASSWORD);
      User savedUser = new User(USERNAME, EMAIL, PASSWORD, null);
      UserDto expectedDto = new UserDto(USER_ID, USERNAME, EMAIL, null, false);

      given(userRepository.existsByEmail(EMAIL)).willReturn(false);
      given(userRepository.existsByUsername(USERNAME)).willReturn(false);
      given(userRepository.save(any(User.class))).willReturn(savedUser);
      given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

      // when
      UserDto result = sut.create(request, Optional.empty());

      // then
      assertThat(result).isEqualTo(expectedDto);
      then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("실패: 이메일 중복 시 IllegalArgumentException")
    void create_fail_duplicateEmail() {
      // given
      UserCreateRequest request = new UserCreateRequest(USERNAME, EMAIL, PASSWORD);
      given(userRepository.existsByEmail(EMAIL)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> sut.create(request, Optional.empty()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("already exists");
      then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("실패: 사용자명 중복 시 IllegalArgumentException")
    void create_fail_duplicateUsername() {
      // given
      UserCreateRequest request = new UserCreateRequest(USERNAME, EMAIL, PASSWORD);
      given(userRepository.existsByEmail(EMAIL)).willReturn(false);
      given(userRepository.existsByUsername(USERNAME)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> sut.create(request, Optional.empty()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("already exists");
      then(userRepository).should(never()).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("성공: 사용자 수정 후 UserDto 반환")
    void update_success() {
      // given
      User user = new User(USERNAME, EMAIL, PASSWORD, null);
      UserUpdateRequest updateRequest = new UserUpdateRequest("newuser", "new@example.com",
          "newpass");
      UserDto expectedDto = new UserDto(USER_ID, "newuser", "new@example.com", null, false);

      given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
      given(userRepository.existsByEmail("new@example.com")).willReturn(false);
      given(userRepository.existsByUsername("newuser")).willReturn(false);
      given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

      // when
      UserDto result = sut.update(USER_ID, updateRequest, Optional.empty());

      // then
      assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("실패: 대상 사용자 없음 시 NoSuchElementException")
    void update_fail_userNotFound() {
      // given
      UserUpdateRequest updateRequest = new UserUpdateRequest("newuser", "new@example.com",
          "newpass");
      given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> sut.update(USER_ID, updateRequest, Optional.empty()))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("실패: 수정 이메일 중복 시 IllegalArgumentException")
    void update_fail_duplicateEmail() {
      // given
      User user = new User(USERNAME, EMAIL, PASSWORD, null);
      UserUpdateRequest updateRequest = new UserUpdateRequest("newuser", "other@example.com", null);
      given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
      given(userRepository.existsByEmail("other@example.com")).willReturn(true);

      // when & then
      assertThatThrownBy(() -> sut.update(USER_ID, updateRequest, Optional.empty()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("already exists");
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("성공: 사용자 삭제 시 deleteById 호출")
    void delete_success() {
      // given
      given(userRepository.existsById(USER_ID)).willReturn(true);

      // when
      sut.delete(USER_ID);

      // then
      then(userRepository).should().deleteById(USER_ID);
    }

    @Test
    @DisplayName("실패: 대상 사용자 없음 시 NoSuchElementException")
    void delete_fail_userNotFound() {
      // given
      given(userRepository.existsById(USER_ID)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> sut.delete(USER_ID))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("not found");
      then(userRepository).should(never()).deleteById(any(UUID.class));
    }
  }
}
