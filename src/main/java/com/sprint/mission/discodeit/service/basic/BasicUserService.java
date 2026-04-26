package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final PasswordEncoder passwordEncoder;
  private final JwtRegistry jwtRegistry;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  @CacheEvict(value = "userList", allEntries = true) // 사용자 추가 시 전체 목록 캐시 무효화
  public UserDto create(UserCreateRequest request, BinaryContentCreateRequest profileRequest) {

    log.info("[USER] create start username={}, email={}", request.username(), request.email());

    // 중복 검사
    validateNewUser(request.username(), request.email());

    // 프로필 생성
    BinaryContent profile = createBinaryContent(profileRequest);

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.password());

    // User 생성
    User user = new User(request.username(), request.email(), encodedPassword, profile,
        UserRole.USER);

    User savedUser = userRepository.save(user);

    log.info("[User] create success userId={}", savedUser.getId());
    return userMapper.toDto(user, isUserOnline(savedUser.getId()));
  }

  @Override
  public UserDto findUser(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    return userMapper.toDto(user, isUserOnline(user.getId()));
  }

  @Override
  @Cacheable(value = "userList") // 조회 시 캐시 저장
  public List<UserDto> findAll() {
    log.info("[CACHE_MISS] DB에서 사용자 목록을 조회합니다.");
    return userRepository.findAll().stream()
        .map(user -> userMapper.toDto(user, isUserOnline(user.getId())))
        .toList();
  }

  @Override
  @Transactional
  @PreAuthorize("#userId == authentication.principal.userDto.id")
  @CacheEvict(value = "userList", allEntries = true)
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

    // 비밀번호 암호화
    String passwordToUpdate = user.getPassword();
    if (request.newPassword() != null && !request.newPassword().isBlank()) {
      passwordToUpdate = passwordEncoder.encode(request.newPassword());
    }

    // 필드 업데이트 및 저장
    user.update(request.newUsername(), request.newEmail(), passwordToUpdate, newProfile);

    log.info("[User] update success userId={}", user.getId());
    return userMapper.toDto(user, isUserOnline(user.getId()));
  }

  @Override
  @Transactional
  @PreAuthorize("#id == authentication.principal.userDto.id")
  @CacheEvict(value = "userList", allEntries = true) // 삭제 시 무효화
  public void delete(UUID id) {
    log.info("[USER] delete start userId={}", id);

    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }

    log.info("[User] delete success userId={}", id);
    userRepository.deleteById(id);
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  @CacheEvict(value = "userList", allEntries = true)
  public UserDto updateRole(UUID userId, UserRole newRole) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    // 권한이 이전과 같으면 로직 수행 X -> 즉시 반환
    if (user.getRole().equals(newRole)) {
      log.info("동일한 권한으로의 변경 요청 - userId: {}", userId);
      return userMapper.toDto(user, isUserOnline(user.getId()));
    }

    // 변경 전 권한 (로그용)
    UserRole oldRole = user.getRole();

    user.updateRole(newRole);

    log.info("[AUTH_CHANGE] 권한 변경 - userId: {}, {} → {}", userId, oldRole, newRole);

    eventPublisher.publishEvent(new RoleUpdatedEvent(userId, oldRole, newRole));

    jwtRegistry.invalidateJwtInformationByUserId(userId);
    return userMapper.toDto(user, isUserOnline(userId));
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

  // 온라인 여부 판단
  private boolean isUserOnline(UUID userId) {
    return jwtRegistry.hasActiveJwtInformationByUserId(userId);
  }
}
