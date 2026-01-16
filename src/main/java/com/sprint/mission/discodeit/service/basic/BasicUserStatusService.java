package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  private final UserStatusMapper userStatusMapper;


  @Override
  @Transactional
  public UserStatusDto create(UserStatusCreateRequest request) {

    // User 엔티티 조회 (UserStatus는 User 엔티티 참조 해야 한다.)
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다. " + request.userId()));

    // 동일 User에 대한 UserStatus 중복 체크
    userStatusRepository.findByUser_Id(request.userId())
        .ifPresent(us -> {
          throw new IllegalStateException("해당 User에 대한 UserStatus가 이미 존재합니다.");
        });

    Instant lastConn = (request.lastActiveAt() != null)
        ? request.lastActiveAt()
        : Instant.now();

    UserStatus status = new UserStatus(user, lastConn);

    UserStatus savedStatus = userStatusRepository.save(status);

    return userStatusMapper.toDto(savedStatus);
  }

  @Override
  public UserStatusDto find(UUID id) {
    return userStatusRepository.findById(id)
        .map(userStatusMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다. " + id));
  }

  @Override
  public List<UserStatusDto> findAll() {
    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public UserStatusDto update(UUID userStatusId, UserStatusUpdateRequest request) {
    UserStatus status = userStatusRepository.findById(userStatusId)
        .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다. " + userStatusId));

    status.update(request.newLastActiveAt());

    return userStatusMapper.toDto(status);
  }

  @Override
  @Transactional
  public UserStatusDto updateByUserId(UUID userId, Instant lastConnAt) {
    UserStatus status = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다: " + userId));

    status.update(lastConnAt != null ? lastConnAt : Instant.now());

    return userStatusMapper.toDto(status);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!userStatusRepository.existsById(id)) {
      throw new NoSuchElementException("UserStatus를 찾을 수 없습니다: " + id);
    }
    userStatusRepository.deleteById(id);
  }
}
