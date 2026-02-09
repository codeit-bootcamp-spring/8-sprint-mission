package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final BinaryContentService binaryContentService;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  @Transactional
  public UserDto create(UserCreateRequest request, BinaryContentCreateRequest profileRequest) {

    log.info("[USER] create start username={}, email={}", request.username(), request.email());

    // 중복 검사
    validateNewUser(request.username(), request.email());

    // 프로필 생성
    BinaryContent profile = createBinaryContent(profileRequest);

    // User 생성
    User user = new User(request.username(), request.email(), request.password(), profile);

    // UserStatus 생성 (마지막 접속 시간 = 지금)
    UserStatus status = new UserStatus(user, Instant.now());

    user.attachStatus(status);

    User savedUser = userRepository.save(user);

    log.info("[User] create success userId={}", savedUser.getId());
    return userMapper.toDto(savedUser);
  }

  @Override
  public UserDto findUser(UUID id) {
    return userRepository.findById(id)
        .map(userMapper::toDto)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(userMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public UserDto update(UUID userId, UserUpdateRequest request,
      BinaryContentCreateRequest profileRequest) {

    log.info("[USER] update start username={}, email={}", request.newUsername(),
        request.newEmail());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    // 수정 정보 중복 체크
    validateUpdateUser(user, request);

    // 프로필 이미지 교체
    BinaryContent newProfile = createBinaryContent(profileRequest);

    // 필드 업데이트 및 저장
    user.update(request.newUsername(), request.newEmail(), request.newPassword(), newProfile);

    log.info("[User] update success userId={}", user.getId());
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.info("[USER] delete start userId={}", id);

    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }

    log.info("[User] delete success userId={}", id);
    userRepository.deleteById(id);
  }

  // 비즈니스 로직 헬퍼 메서드
  private void validateNewUser(String username, String email) {
    if (userRepository.existsByUsernameOrEmail(username, email)) {
      throw new UserAlreadyExistsException(username, email);
    }
  }

  private void validateUpdateUser(User user, UserUpdateRequest request) {
    if (request.newUsername() != null && !request.newUsername().equals(user.getUsername())) {
      userRepository.findByUsername(request.newUsername())
          .filter(u -> !u.getId().equals(user.getId()))
          .ifPresent(other -> {
            throw new UserAlreadyExistsException(request.newUsername(), request.newEmail());
          });
    }

    if (request.newEmail() != null && !request.newEmail().equals(user.getEmail())) {
      userRepository.findByEmail(request.newEmail())
          .filter(u -> !u.getId().equals(user.getId()))
          .ifPresent(u -> {
            throw new UserAlreadyExistsException(request.newUsername(), request.newEmail());
          });
    }
  }

  private BinaryContent createBinaryContent(BinaryContentCreateRequest request) {

    if (request == null || request.bytes() == null) {
      return null;
    }

    BinaryContentDto dto = binaryContentService.create(request);

    return binaryContentRepository.findById(dto.id())
        .orElseThrow(
            () -> new BinaryContentNotFoundException(dto.id()));
  }
}
