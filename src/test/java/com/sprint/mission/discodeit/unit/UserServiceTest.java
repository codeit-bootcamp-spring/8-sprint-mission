package com.sprint.mission.discodeit.unit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveFailedException;
import com.sprint.mission.discodeit.exception.user.UserEmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UsernameAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @InjectMocks
  private BasicUserService basicUserService;

  UUID userId;
  String username;
  String email;
  String password;
  User mockUser;
  UserDto mockUserDto;
  UUID profileId;
  byte[] profileBytes;
  BinaryContent mockProfile;
  BinaryContentDto mockProfileDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    username = "testUser";
    email = "test@naver.com";
    password = "test1234";

    profileId = UUID.randomUUID();
    profileBytes = new byte[]{1, 2, 3, 4, 5};
    mockProfile = new BinaryContent("profile.png", (long) profileBytes.length, "image/png");
    mockProfileDto = new BinaryContentDto(profileId, "profile.png", (long) profileBytes.length,
        "image/png");
    mockUser = new User(username, email, password, mockProfile);
    mockUserDto = new UserDto(userId, username, email, mockProfileDto, true, Role.USER);
  }

  @Nested
  @DisplayName("사용자 생성 관련 테스트")
  class UserCreateTests {

    @Test
    @DisplayName("사용자 생성 성공인 경우")
    void create_ShouldReturnUserDto() {

      // given
      UserCreateRequest userReq = new UserCreateRequest(username, email, password);
      BinaryContentCreateRequest profileReq = new BinaryContentCreateRequest("profile.png",
          "png", profileBytes);

      given(userRepository.existsByUsername(eq(username))).willReturn(false);
      given(userRepository.existsByEmail(eq(email))).willReturn(false);

      given(binaryContentRepository.save(any(BinaryContent.class))).willAnswer(invocation -> {
        BinaryContent content = invocation.getArgument(0);
        setField(content, "id", profileId);
        return content;
      });
      given(binaryContentStorage.put(eq(profileId), any(byte[].class))).willReturn(profileId);
      given(userMapper.toDto(any(User.class))).willReturn(mockUserDto);

      // when
      UserDto result = basicUserService.create(userReq, Optional.of(profileReq));

      // then
      assertThat(result).isEqualTo(mockUserDto);

      then(userRepository).should().save(any(User.class));
      then(binaryContentRepository).should().save(any(BinaryContent.class));
      then(binaryContentStorage).should().put(eq(profileId), eq(profileBytes));
    }

    @Test
    @DisplayName("사용자 생성 실패 - 중복된 이름인 경우")
    void create_WhenDuplicateName_ShouldThrowException() {

      // given
      UserCreateRequest userReq = new UserCreateRequest(username, email, password);
      BinaryContentCreateRequest profileReq = new BinaryContentCreateRequest("profile.png",
          "png", profileBytes);

      given(userRepository.existsByUsername(eq(userReq.username()))).willReturn(true);

      // when
      assertThatThrownBy(() -> basicUserService.create(userReq, Optional.of(profileReq)))
          .isInstanceOf(UsernameAlreadyExistsException.class);

      // then
      then(userRepository).should(never()).save(any(User.class));
      then(userRepository).should(never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("사용자 생성 실패 - 중복된 이메일인 경우")
    void create_WhenDuplicateEmail_ShouldThrowException() {

      // given
      UserCreateRequest userReq = new UserCreateRequest(username, email, password);
      BinaryContentCreateRequest profileReq = new BinaryContentCreateRequest("profile.png",
          "png", profileBytes);

      given(userRepository.existsByEmail(eq(userReq.email()))).willReturn(true);

      // when
      assertThatThrownBy(() -> basicUserService.create(userReq, Optional.of(profileReq)))
          .isInstanceOf(UserEmailAlreadyExistsException.class);

      // then
      then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 실패 - 프로필 저장 중 스토리지 에러 발생")
    void create_WhenStorageFail_ThrowException() {

      // given
      BinaryContentCreateRequest profileReq = new BinaryContentCreateRequest("profile.png",
          "png", profileBytes);
      UserCreateRequest userReq = new UserCreateRequest(username, email, password);
      BinaryContent profile = new BinaryContent("profile.png", (long) profileBytes.length,
          "image/png");

      given(userRepository.existsByUsername(anyString())).willReturn(false);
      given(userRepository.existsByEmail(anyString())).willReturn(false);

      given(binaryContentRepository.save(any(BinaryContent.class))).willAnswer(invocation -> {
        BinaryContent content = invocation.getArgument(0);
        setField(content, "id", profileId);
        return content;
      });

      doThrow(new RuntimeException()).when(binaryContentStorage).put(any(), any());

      assertThrows(BinaryContentSaveFailedException.class,
          () -> basicUserService.create(userReq, Optional.of(profileReq)));

      then(binaryContentStorage).should().put(any(), any());
    }
  }

  @Nested
  @DisplayName("사용자 조회 관련 테스트")
  class UserFindTests {

    @Test
    @DisplayName("사용자 조회 성공")
    void find_ShouldReturnUserDto() {

      //given
      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(userMapper.toDto(mockUser)).willReturn(mockUserDto);

      // when
      UserDto result = basicUserService.find(userId);

      // then
      assertThat(result).isEqualTo(mockUserDto);
      assertThat(result.id()).isEqualTo(userId);
    }

    @Test
    @DisplayName("사용자 조회 실패 - 사용자를 찾지 못 했을 경우")
    void find_WhenNotFoundUser_ShouldThrowException() {

      // given
      UUID nonExistUserId = UUID.randomUUID();

      given(userRepository.findById(nonExistUserId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> basicUserService.find(nonExistUserId))
          .isInstanceOf(UserNotFoundException.class);

      then(userRepository).should().findById(eq(nonExistUserId));
    }
  }

  @Nested
  @DisplayName("사용자 수정 관련 테스트")
  class UserUpdateTests {

    @Test
    @DisplayName("사용자 수정 성공")
    void update_ShouldReturnUserDto() {

      // given
      UserUpdateRequest userReq = new UserUpdateRequest("newTestUser", "newTestEmail@naver.com",
          "test1234");
      BinaryContentCreateRequest profileReq = new BinaryContentCreateRequest("profile.png",
          "image/png", "profile.png".getBytes());
      UUID profileId = UUID.randomUUID();
      mockProfileDto = new BinaryContentDto(profileId, "profile.png", 10L, "iamge/png");

      given(userRepository.findById(eq(userId))).willReturn(Optional.of(mockUser));
      given(userRepository.existsByUsername(eq(userReq.newUsername()))).willReturn(false);
      given(userRepository.existsByEmail(eq(userReq.newEmail()))).willReturn(false);

      given(binaryContentRepository.save(any(BinaryContent.class))).willAnswer(invocation -> {
        BinaryContent content = invocation.getArgument(0);
        ReflectionTestUtils.setField(content, "id", profileId);
        return content;
      });

      UserDto updatedDto = new UserDto(userId, userReq.newUsername(), userReq.newEmail(),
          mockProfileDto, true, Role.USER);

      given(userMapper.toDto(any(User.class))).willReturn(updatedDto);

      // when
      UserDto result = basicUserService.update(userId, userReq, Optional.of(profileReq));

      // then
      assertThat(result).isEqualTo(updatedDto);

      then(userRepository).should().findById(eq(userId));
      then(binaryContentRepository).should().save(any(BinaryContent.class));
      then(binaryContentStorage).should().put(any(), any());
    }

    @Test
    @DisplayName("사용자 수정 실패 - 사용자를 찾지 못 했을 경우")
    void update_WhenNotFoundUser_ShouldThrowException() {

      // given
      UserUpdateRequest userReq = new UserUpdateRequest("newTestUser", "newTestEmail@naver.com",
          "test1234");

      // when & then
      given(userRepository.findById(eq(userId))).willReturn(Optional.empty());
      assertThatThrownBy(() -> basicUserService.update(userId, userReq, Optional.empty()))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 수정 실패 -  종복된 이름이 존재하는 경우")
    void update_WhenDuplicateName_ThrowException() {

      // given
      UserUpdateRequest userReq = new UserUpdateRequest("newTestUser", "newTestEmail@naver.com",
          "test1234");

      // when & then
      given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(mockUser));
      given(userRepository.existsByUsername(anyString())).willReturn(true);
      assertThatThrownBy(() -> basicUserService.update(userId, userReq, Optional.empty()))
          .isInstanceOf(UsernameAlreadyExistsException.class);

      then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 수정 실패 -  종복된 이메일이 존재하는 경우")
    void update_WhenDuplicateEmail_ThrowException() {

      // given
      UserUpdateRequest userReq = new UserUpdateRequest("newTestUser", "newTestEmail@naver.com",
          "test1234");

      // when & then
      given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(mockUser));
      given(userRepository.existsByEmail(anyString())).willReturn(true);
      assertThatThrownBy(() -> basicUserService.update(userId, userReq, Optional.empty()))
          .isInstanceOf(UserEmailAlreadyExistsException.class);

      then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 수정 실패 - 새 프로필 저장 중 스토리지 에러 발생")
    void update_WhenStorageFail_ThrowException() {

      // given
      UserUpdateRequest userReq = new UserUpdateRequest("newTestUser", "newTestEmail@naver.com",
          "test1234");
      BinaryContentCreateRequest profileReq = new BinaryContentCreateRequest("new.png",
          "image/png", profileBytes);

      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(userRepository.existsByUsername(anyString())).willReturn(false);
      given(userRepository.existsByEmail(anyString())).willReturn(false);

      given(binaryContentRepository.save(any(BinaryContent.class))).willAnswer(invocation -> {
        BinaryContent content = invocation.getArgument(0);
        ReflectionTestUtils.setField(content, "id", UUID.randomUUID());
        return content;
      });

      doThrow(new RuntimeException()).when(binaryContentStorage).put(any(), any());

      assertThrows(BinaryContentSaveFailedException.class,
          () -> basicUserService.update(userId, userReq, Optional.of(profileReq)));
    }
  }

  @Nested
  @DisplayName("사용자 삭제 관련 테스트")
  class UserDeleteTests {

    @Test
    @DisplayName("사용자 삭제 성공인 경우")
    void delete_ShouldSuccess() {

      // given
      given(userRepository.findById(eq(userId))).willReturn(Optional.of(mockUser));

      // when
      basicUserService.delete(userId);

      // then
      then(userRepository).should().findById(eq(userId));
      then(userRepository).should().deleteById(eq(userId));
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 사용자를 찾지 못 했을 경우")
    void delete_WhenNotFoundUser_ShouldThrowException() {

      // given
      given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> basicUserService.delete(userId)).isInstanceOf(
          UserNotFoundException.class);

      then(userRepository).should(never()).delete(any(User.class));
    }
  }
}
