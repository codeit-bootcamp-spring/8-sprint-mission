package com.sprint.mission.discodeit.service.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class BasicUserStatusServiceTest {

  @Mock
  UserStatusRepository userStatusRepository;

  @Mock
  UserRepository userRepository;

  @Mock
  UserStatusMapper userStatusMapper;

  @InjectMocks
  BasicUserStatusService userStatusService;

  @Test
  @DisplayName("create 성공: user 존재 + 중복 없음 -> 저장 후 mapper 변환")
  void create_success_savesAndReturnsDto() {
    // given
    UUID userId = UUID.randomUUID();
    User user = new User("junyoung", "jun@test.com", "pw", null);
    ReflectionTestUtils.setField(user, "id", userId);

    Instant lastActiveAt = Instant.parse("2025-01-01T00:00:00Z");
    UserStatusCreateRequest req = new UserStatusCreateRequest(userId, lastActiveAt);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userStatusRepository.findByUser_Id(userId)).thenReturn(Optional.empty());

    // save로 넘어가는 엔티티를 검증하기 위해 captor 사용
    ArgumentCaptor<UserStatus> captor = ArgumentCaptor.forClass(UserStatus.class);

    UserStatus saved = new UserStatus(user, lastActiveAt);
    UUID statusId = UUID.randomUUID();
    ReflectionTestUtils.setField(saved, "id", statusId);

    when(userStatusRepository.save(any(UserStatus.class))).thenReturn(saved);

    UserStatusDto mapped = new UserStatusDto(statusId, userId, lastActiveAt);
    when(userStatusMapper.toDto(saved)).thenReturn(mapped);

    // when
    UserStatusDto result = userStatusService.create(req);

    // then
    assertSame(mapped, result);

    verify(userRepository).findById(userId);
    verify(userStatusRepository).findByUser_Id(userId);
    verify(userStatusRepository).save(captor.capture());

    UserStatus toSave = captor.getValue();
    assertSame(user, toSave.getUser());
    assertEquals(lastActiveAt, toSave.getLastActiveAt());

    verify(userStatusMapper).toDto(saved);
    verifyNoMoreInteractions(userRepository, userStatusRepository, userStatusMapper);
  }

  @Test
  @DisplayName("create 실패: user 없으면 UserNotFoundException")
  void create_fail_userNotFound() {
    // given
    UUID userId = UUID.randomUUID();
    UserStatusCreateRequest req = new UserStatusCreateRequest(userId, Instant.now());

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(UserNotFoundException.class, () -> userStatusService.create(req));

    verify(userRepository).findById(userId);
    verifyNoMoreInteractions(userRepository, userStatusRepository, userStatusMapper);
  }

  @Test
  @DisplayName("create 실패: 이미 해당 user의 status가 존재하면 UserStatusAlreadyExistsException")
  void create_fail_duplicate() {
    // given
    UUID userId = UUID.randomUUID();
    User user = new User("junyoung", "jun@test.com", "pw", null);
    ReflectionTestUtils.setField(user, "id", userId);

    UserStatusCreateRequest req = new UserStatusCreateRequest(userId, Instant.now());

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userStatusRepository.findByUser_Id(userId))
        .thenReturn(Optional.of(new UserStatus(user, Instant.now())));

    // when / then
    assertThrows(UserStatusAlreadyExistsException.class, () -> userStatusService.create(req));

    verify(userRepository).findById(userId);
    verify(userStatusRepository).findByUser_Id(userId);
    verify(userStatusRepository, never()).save(any());
    verifyNoMoreInteractions(userRepository, userStatusRepository, userStatusMapper);
  }

  @Test
  @DisplayName("find 성공: 존재하면 mapper 변환된 dto 반환")
  void find_success() {
    // given
    UUID statusId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = new User("junyoung", "jun@test.com", "pw", null);
    ReflectionTestUtils.setField(user, "id", userId);

    Instant last = Instant.parse("2025-01-01T00:00:00Z");
    UserStatus status = new UserStatus(user, last);
    ReflectionTestUtils.setField(status, "id", statusId);

    when(userStatusRepository.findById(statusId)).thenReturn(Optional.of(status));

    UserStatusDto mapped = new UserStatusDto(statusId, userId, last);
    when(userStatusMapper.toDto(status)).thenReturn(mapped);

    // when
    UserStatusDto result = userStatusService.find(statusId);

    // then
    assertSame(mapped, result);
    verify(userStatusRepository).findById(statusId);
    verify(userStatusMapper).toDto(status);
    verifyNoMoreInteractions(userStatusRepository, userStatusMapper, userRepository);
  }

  @Test
  @DisplayName("find 실패: 없으면 UserStatusNotFoundException")
  void find_fail_notFound() {
    // given
    UUID statusId = UUID.randomUUID();
    when(userStatusRepository.findById(statusId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(UserStatusNotFoundException.class, () -> userStatusService.find(statusId));

    verify(userStatusRepository).findById(statusId);
    verifyNoMoreInteractions(userStatusRepository, userStatusMapper, userRepository);
  }

  @Test
  @DisplayName("findAll: repository 결과를 전부 mapper로 변환해서 반환")
  void findAll_mapsAll() {
    // given
    UUID userId = UUID.randomUUID();
    User user = new User("junyoung", "jun@test.com", "pw", null);
    ReflectionTestUtils.setField(user, "id", userId);

    UserStatus s1 = new UserStatus(user, Instant.EPOCH);
    UserStatus s2 = new UserStatus(user, Instant.EPOCH.plusSeconds(10));

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    ReflectionTestUtils.setField(s1, "id", id1);
    ReflectionTestUtils.setField(s2, "id", id2);

    when(userStatusRepository.findAll()).thenReturn(List.of(s1, s2));

    UserStatusDto d1 = new UserStatusDto(id1, userId, s1.getLastActiveAt());
    UserStatusDto d2 = new UserStatusDto(id2, userId, s2.getLastActiveAt());
    when(userStatusMapper.toDto(s1)).thenReturn(d1);
    when(userStatusMapper.toDto(s2)).thenReturn(d2);

    // when
    List<UserStatusDto> result = userStatusService.findAll();

    // then
    assertEquals(2, result.size());
    assertSame(d1, result.get(0));
    assertSame(d2, result.get(1));

    verify(userStatusRepository).findAll();
    verify(userStatusMapper).toDto(s1);
    verify(userStatusMapper).toDto(s2);
    verifyNoMoreInteractions(userStatusRepository, userStatusMapper, userRepository);
  }

  @Test
  @DisplayName("update 성공: status 찾아서 lastActiveAt 갱신 후 mapper 변환")
  void update_success_updatesLastActiveAt() {
    // given
    UUID statusId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = new User("junyoung", "jun@test.com", "pw", null);
    ReflectionTestUtils.setField(user, "id", userId);

    UserStatus status = new UserStatus(user, Instant.EPOCH);
    ReflectionTestUtils.setField(status, "id", statusId);

    when(userStatusRepository.findById(statusId)).thenReturn(Optional.of(status));

    Instant newAt = Instant.parse("2025-02-01T00:00:00Z");
    UserStatusUpdateRequest req = new UserStatusUpdateRequest(newAt);

    UserStatusDto mapped = new UserStatusDto(statusId, userId, newAt);
    when(userStatusMapper.toDto(status)).thenReturn(mapped);

    // when
    UserStatusDto result = userStatusService.update(statusId, req);

    // then
    assertSame(mapped, result);
    assertEquals(newAt, status.getLastActiveAt());

    verify(userStatusRepository).findById(statusId);
    verify(userStatusMapper).toDto(status);
    verifyNoMoreInteractions(userStatusRepository, userStatusMapper, userRepository);
  }

  @Test
  @DisplayName("update 실패: status 없으면 UserStatusNotFoundException")
  void update_fail_notFound() {
    // given
    UUID statusId = UUID.randomUUID();
    when(userStatusRepository.findById(statusId)).thenReturn(Optional.empty());

    UserStatusUpdateRequest req = new UserStatusUpdateRequest(Instant.now());

    // when / then
    assertThrows(UserStatusNotFoundException.class, () -> userStatusService.update(statusId, req));

    verify(userStatusRepository).findById(statusId);
    verifyNoMoreInteractions(userStatusRepository, userStatusMapper, userRepository);
  }

  @Test
  @DisplayName("updateByUserId 성공: 기존 status 있으면 가져와서 update 후 mapper 변환 (save 호출 없음)")
  void updateByUserId_existing_status_updates() {
    // given
    UUID userId = UUID.randomUUID();

    User user = new User("junyoung", "jun@test.com", "pw", null);
    ReflectionTestUtils.setField(user, "id", userId);

    UserStatus status = new UserStatus(user, Instant.EPOCH);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userStatusRepository.findByUser_Id(userId)).thenReturn(Optional.of(status));

    Instant newAt = Instant.parse("2025-03-01T00:00:00Z");
    UserStatusDto mapped = new UserStatusDto(null, userId, newAt);
    when(userStatusMapper.toDto(status)).thenReturn(mapped);

    // when
    UserStatusDto result = userStatusService.updateByUserId(userId, newAt);

    // then
    assertSame(mapped, result);
    assertEquals(newAt, status.getLastActiveAt());

    verify(userRepository).findById(userId);
    verify(userStatusRepository).findByUser_Id(userId);
    verify(userStatusMapper).toDto(status);

    verify(userStatusRepository, never()).save(any());
    verifyNoMoreInteractions(userRepository, userStatusRepository, userStatusMapper);
  }

  @Test
  @DisplayName("updateByUserId 성공: status 없으면 새로 생성해서 update 후 mapper 변환 (save 호출 없음)")
  void updateByUserId_missing_status_createsNew() {
    // given
    UUID userId = UUID.randomUUID();

    User user = new User("junyoung", "jun@test.com", "pw", null);
    ReflectionTestUtils.setField(user, "id", userId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userStatusRepository.findByUser_Id(userId)).thenReturn(Optional.empty());

    Instant newAt = Instant.parse("2025-04-01T00:00:00Z");

    ArgumentCaptor<UserStatus> captor = ArgumentCaptor.forClass(UserStatus.class);
    UserStatusDto mapped = new UserStatusDto(null, userId, newAt);

    when(userStatusMapper.toDto(any(UserStatus.class))).thenReturn(mapped);

    // when
    UserStatusDto result = userStatusService.updateByUserId(userId, newAt);

    // then
    assertSame(mapped, result);

    verify(userRepository).findById(userId);
    verify(userStatusRepository).findByUser_Id(userId);

    verify(userStatusMapper).toDto(captor.capture());
    UserStatus created = captor.getValue();

    assertNotNull(created);
    assertSame(user, created.getUser());
    assertEquals(newAt, created.getLastActiveAt());

    verify(userStatusRepository, never()).save(any());
    verifyNoMoreInteractions(userRepository, userStatusRepository, userStatusMapper);
  }

  @Test
  @DisplayName("updateByUserId 실패: user 없으면 UserNotFoundException")
  void updateByUserId_fail_userNotFound() {
    // given
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(UserNotFoundException.class,
        () -> userStatusService.updateByUserId(userId, Instant.now()));

    verify(userRepository).findById(userId);
    verifyNoMoreInteractions(userRepository, userStatusRepository, userStatusMapper);
  }

  @Test
  @DisplayName("delete 성공: 존재하면 deleteById 호출")
  void delete_success() {
    // given
    UUID statusId = UUID.randomUUID();
    when(userStatusRepository.existsById(statusId)).thenReturn(true);

    // when
    userStatusService.delete(statusId);

    // then
    verify(userStatusRepository).existsById(statusId);
    verify(userStatusRepository).deleteById(statusId);
    verifyNoMoreInteractions(userStatusRepository, userStatusMapper, userRepository);
  }

  @Test
  @DisplayName("delete 실패: 없으면 UserStatusNotFoundException")
  void delete_fail_notFound() {
    // given
    UUID statusId = UUID.randomUUID();
    when(userStatusRepository.existsById(statusId)).thenReturn(false);

    // when / then
    assertThrows(UserStatusNotFoundException.class, () -> userStatusService.delete(statusId));

    verify(userStatusRepository).existsById(statusId);
    verify(userStatusRepository, never()).deleteById(any());
    verifyNoMoreInteractions(userStatusRepository, userStatusMapper, userRepository);
  }
}
