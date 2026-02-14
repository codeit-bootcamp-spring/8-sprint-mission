package com.sprint.mission.discodeit.service.basic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class BasicUserServiceTest {

  @Mock
  UserRepository userRepository;
  @Mock
  UserMapper userMapper;
  @Mock
  BinaryContentService binaryContentService;
  @Mock
  BinaryContentRepository binaryContentRepository;

  @InjectMocks
  BasicUserService userService;

  @Test
  @DisplayName("create 성공: 중복이 아니면 userRepository.save 호출되고 UserDto 반환")
  void create_success() {

    // Given
    UserCreateRequest request = new UserCreateRequest("jun", "jun@test.com", "pw12345");
    when(userRepository.existsByUsernameOrEmail("jun", "jun@test.com")).thenReturn(false);

    // userRepository.save 호출 -> 저장 된 User를 반환한다고, 가정
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    // 어떤 User가 오던 내가 원하는 dto를 반환하도록 스텁
    UserDto mapped = new UserDto(UUID.randomUUID(), "jun", "jun@test.com", null, false);
    when(userMapper.toDto(any(User.class))).thenReturn(mapped);

    // When
    UserDto result = userService.create(request, null);

    // Then
    assertSame(mapped, result);
    verify(userRepository).existsByUsernameOrEmail("jun", "jun@test.com");
    verify(userRepository).save(any(User.class));
    verify(userMapper).toDto(any(User.class));
    // profileRequest가 null이기에 binary쪽 service나 repo는 절대 호출 X
    // 내가 앞에서 verify()로 확인한 것들 말고는 추가로 호출된 게 없어야 한다"
    verifyNoMoreInteractions(binaryContentService, binaryContentRepository);
  }

  @Test
  @DisplayName("create 실패: username/email 중복이면 예외 발생 + save는 호출되지 않음")
  void create_fail_duplicate() {

    // Given
    UserCreateRequest request = new UserCreateRequest("jun", "jun@test.com", "pw12345");
    when(userRepository.existsByUsernameOrEmail("jun", "jun@test.com")).thenReturn(true);

    // When & Then
    assertThrows(UserAlreadyExistsException.class, () -> userService.create(request, null));

    verify(userRepository).existsByUsernameOrEmail("jun", "jun@test.com");
    // never() -> 단 한번도 호출 되지 않았음.
    verify(userRepository, never()).save(any());
    verifyNoInteractions(userMapper, binaryContentService, binaryContentRepository);
  }

  @Test
  @DisplayName("update 성공: username/email 변경 시 중복 없으면 엔티티 업데이트 + dto 반환")
  void update_success() {

    // Given
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("newName", "new@test.com", "newPw", null);

    User user = new User("oldName", "old@test.com", "oldPw", null);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.findByUsername("newName")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());

    UserDto mapped = new UserDto(userId, "newName", "new@test.com", null, false);
    when(userMapper.toDto(user)).thenReturn(mapped);

    // When
    UserDto result = userService.update(userId, request, null);

    // Then
    assertSame(mapped, result);
    verify(userRepository).findById(userId);
    verify(userRepository).findByUsername("newName");
    verify(userRepository).findByEmail("new@test.com");
    verify(userRepository, never()).save(any(User.class));
    verify(userMapper).toDto(user);

    assertEquals("newName", user.getUsername());
    assertEquals("new@test.com", user.getEmail());
  }

  @Test
  @DisplayName("update 실패: username이 다른 사용자에게 이미 존재하면 예외 발생")
  void update_fail_duplicateUsername() {

    // Given
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("newName", "new@test.com", "newPw", null);

    User user = new User("oldName", "old@test.com", "oldPw", null);
    ReflectionTestUtils.setField(user, "id", userId);

    User other = new User("newName", "other@test.com", "pw", null);
    ReflectionTestUtils.setField(other, "id", UUID.randomUUID());

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // username 중복 발생
    when(userRepository.findByUsername("newName")).thenReturn(Optional.of(other));

    // When & Then
    assertThrows(UserAlreadyExistsException.class, () -> userService.update(userId, request, null));

    verify(userRepository).findById(userId);
    verify(userRepository).findByUsername("newName");
    verify(userRepository, never()).save(any(User.class));
    verifyNoInteractions(userMapper);
  }

  @Test
  @DisplayName("delete 성공: 존재하면 deleteById 호출")
  void delete_success() {

    // Given
    UUID userId = UUID.randomUUID();
    when(userRepository.existsById(userId)).thenReturn(true);

    // When
    assertDoesNotThrow(() -> userService.delete(userId));

    // Then
    verify(userRepository).existsById(userId);
    verify(userRepository).deleteById(userId);

    verifyNoInteractions(userMapper, binaryContentService, binaryContentRepository);
  }

  @Test
  @DisplayName("delete 실패: 존재하지 않으면 예외 발생")
  void delete_fail_notFound() {

    // Given
    UUID userId = UUID.randomUUID();
    when(userRepository.existsById(userId)).thenReturn(false);

    // When & Then
    assertThrows(UserNotFoundException.class, () -> userService.delete(userId));

    verify(userRepository).existsById(userId);
    verify(userRepository, never()).deleteById(any());
  }


}
