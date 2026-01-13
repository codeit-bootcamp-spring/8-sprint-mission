package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public UserResponse create(UserCreateRequest request, BinaryContentCreateRequest profileRequest) {

    // Username / Email 중복 검증
    validateNewUser(request.username(), request.email());

    // User 생성
    User user = userRepository.save(
        new User(request.username(), request.email(), request.password()));

    // UserStatus 생성 (마지막 접속 시간 = 지금)
    UserStatus status = userStatusRepository.save(new UserStatus(user.getId(), Instant.now()));

    // 프로필 이미지 처리
    UUID profileId = handleProfileImage(user.getId(), null, profileRequest);
    if (profileId != null) {
      user.update(null, null, null, profileId);
      userRepository.save(user); // 프로필 ID 반영을 위한 업데이트 저장
    }

    return UserResponse.of(user, status);
  }

  @Override
  public UserResponse findUser(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다. " + id));

    UserStatus status = userStatusRepository.findByUserId(id)
        .orElse(null);

    return UserResponse.of(user, status);
  }

  @Override
  public List<UserResponse> findAll() {
    return userRepository.findAll().stream()
        .map(user -> {
          UserStatus status = userStatusRepository.findByUserId(user.getId())
              .orElse(null);
          return UserResponse.of(user, status);
        })
        .toList();
  }

  @Override
  public UserResponse update(UUID userId, UserUpdateRequest request,
      BinaryContentCreateRequest profileRequest) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다. " + userId));

    // 수정 정보 중복 체크
    validateUpdateUser(user, request);

    // 프로필 이미지 교체
    UUID newProfileId = handleProfileImage(user.getId(), user.getProfileId(), profileRequest);

    // 필드 업데이트 및 저장
    user.update(request.newUsername(), request.newEmail(), request.newPassword(), newProfileId);
    userRepository.save(user);

    UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
    return UserResponse.of(user, status);
  }

  @Override
  public void delete(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다: " + id));

    // 프로필 이미지 삭제
    if (user.getProfileId() != null) {
      binaryContentRepository.deleteById(user.getProfileId());
    }

    // UserStatus 삭제
    userStatusRepository.findByUserId(id)
        .ifPresent(status -> userStatusRepository.deleteById(status.getId()));

    // User 삭제
    userRepository.delete(id);
  }

  // 비즈니스 로직 헬퍼 메서드
  public void validateNewUser(String username, String email) {
    if (userRepository.existsByUsernameOrEmail(username, email)) {
      throw new IllegalArgumentException("이미 사용 중인 newUsername 또는 newEmail 입니다. 다시 입력 부탁드립니다.");
    }
  }

  public void validateUpdateUser(User user, UserUpdateRequest request) {
    if (request.newUsername() != null && !request.newUsername().equals(user.getName())) {
      userRepository.findByUsername(request.newUsername())
          .filter(u -> !u.getId().equals(user.getId()))
          .ifPresent(other -> {
            throw new IllegalArgumentException("이미 사용 중인 Username 입니다." + request.newUsername());
          });
    }

    if (request.newEmail() != null && !request.newEmail().equals(user.getEmail())) {
      userRepository.findByEmail(request.newEmail())
          .filter(u -> !u.getId().equals(user.getId()))
          .ifPresent(u -> {
            throw new IllegalArgumentException("이미 사용 중인 Email 입니다." + request.newEmail());
          });
    }
  }

  public UUID handleProfileImage(UUID userId, UUID existingProfileId,
      BinaryContentCreateRequest profileRequest) {

    // 이미지가 전달 되지 않은 경우 -> 기존 ID 그대로 반환
    if (profileRequest == null || profileRequest.bytes() == null) {
      return existingProfileId;
    }

    // 기존 이미지 있다면 삭제 처리
    if (existingProfileId != null) {
      binaryContentRepository.deleteById(existingProfileId);
    }

    // 새로운 이미지 저장
    BinaryContent content = new BinaryContent(
        profileRequest.fileName(),
        profileRequest.contentType(),
        profileRequest.bytes()
    );
    return binaryContentRepository.save(content).getId();
  }
}
