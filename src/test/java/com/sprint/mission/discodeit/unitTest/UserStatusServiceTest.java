package com.sprint.mission.discodeit.unitTest;

import com.sprint.mission.discodeit.DTO.dto.UserStatusDto;
import com.sprint.mission.discodeit.DTO.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.UserStatusException.UserStatusUserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserStatusServiceTest {

  @InjectMocks
  private BasicUserStatusService userStatusService;

  @Mock
  private UserStatusRepository userStatusRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private UserStatusMapper userStatusMapper;

  private User user;
  private UserStatus userStatus;

  @BeforeEach
  void setUp() {
    user = new User("testUser", "test@codeit.com", "password123", null);
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

    userStatus = new UserStatus(user, Instant.now());
    ReflectionTestUtils.setField(userStatus, "id", UUID.randomUUID());
  }

  @Test
  @DisplayName("사용자 상태 생성 성공")
  void createUserStatus_success() {
    // given
    UserStatusCreateRequest request = new UserStatusCreateRequest(user.getId(), Instant.now());
    UserStatusDto dto = new UserStatusDto(userStatus.getId(), user.getId(),
        userStatus.getLastActiveAt());

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(userStatusRepository.findByUserId(user.getId())).willReturn(Optional.empty());
    given(userStatusRepository.save(any(UserStatus.class))).willReturn(userStatus);
    given(userStatusMapper.toDto(any(UserStatus.class))).willReturn(dto);

    // when
    UserStatusDto result = userStatusService.create(request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.userId()).isEqualTo(user.getId());
    then(userStatusRepository).should().save(any(UserStatus.class));
  }

  @Test
  @DisplayName("사용자 ID로 상태 업데이트 성공")
  void updateByUserId_success() {
    // given
    Instant newTime = Instant.now().plusSeconds(120);
    UserStatusUpdateRequest request = new UserStatusUpdateRequest(newTime);
    UserStatusDto dto = new UserStatusDto(userStatus.getId(), user.getId(), newTime);

    given(userStatusRepository.findByUserId(user.getId())).willReturn(Optional.of(userStatus));
    given(userStatusRepository.save(any(UserStatus.class))).willReturn(userStatus);
    given(userStatusMapper.toDto(any(UserStatus.class))).willReturn(dto);

    // when
    UserStatusDto result = userStatusService.updateByUserId(user.getId(), request);

    // then
    assertThat(result.lastActiveAt()).isEqualTo(newTime);
    then(userStatusRepository).should().save(any(UserStatus.class));
  }

  @Test
  @DisplayName("사용자 ID로 상태 업데이트 실패 - 존재하지 않는 사용자 상태")
  void updateByUserId_fail_notFound() {
    // given
    UUID nonExistentUserId = UUID.randomUUID();
    UserStatusUpdateRequest request = new UserStatusUpdateRequest(Instant.now());

    given(userStatusRepository.findByUserId(nonExistentUserId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userStatusService.updateByUserId(nonExistentUserId, request))
        .isInstanceOf(UserStatusUserNotFoundException.class);
  }
}
