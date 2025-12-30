package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;


  @Override
  public UserStatusResponse create(UserStatusCreateRequest request) {

    // User 검증
    if (!userRepository.existsById(request.userId())) {
      throw new NoSuchElementException("User를 찾을 수 없습니다. " + request.userId());
    }

    // 동일 User에 대한 UserStatus 중복 체크
    userStatusRepository.findByUserId(request.userId())
        .ifPresent(us -> {
          throw new IllegalStateException("해당 User에 대한 UserStatus가 이미 존재합니다.");
        });

    Instant lastConn = (request.lastActiveAt() != null)
        ? request.lastActiveAt()
        : Instant.now();

    UserStatus status = new UserStatus(request.userId(), lastConn);
    userStatusRepository.save(status);

    return convertDto(status);
  }

  @Override
  public UserStatusResponse find(UUID id) {
    UserStatus status = userStatusRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다. " + id));

    return convertDto(status);
  }

  @Override
  public List<UserStatusResponse> findAll() {
    return userStatusRepository.findAll().stream()
        .map(this::convertDto)
        .collect(Collectors.toList());
  }

  @Override
  public UserStatusResponse update(UUID userStatusId, UserStatusUpdateRequest request) {
    UserStatus status = userStatusRepository.findById(userStatusId)
        .orElseThrow(
            () -> new NoSuchElementException("UserStatus를 찾을 수 없습니다. " + userStatusId));

    status.update(request.newLastActiveAt());
    userStatusRepository.save(status);

    return convertDto(status);
  }

  @Override
  public UserStatusResponse updateByUserId(UUID userId, Instant lastConnAt) {
    UserStatus status = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다: " + userId));

    status.update(lastConnAt != null ? lastConnAt : Instant.now());
    userStatusRepository.save(status);

    return convertDto(status);
  }

  @Override
  public void delete(UUID id) {
    userStatusRepository.deleteById(id);
  }

  private UserStatusResponse convertDto(UserStatus status) {
    return new UserStatusResponse(
        status.getId(),
        status.getUserId(),
        status.getLastActiveAt(),
        status.isOnline()
    );
  }
}
