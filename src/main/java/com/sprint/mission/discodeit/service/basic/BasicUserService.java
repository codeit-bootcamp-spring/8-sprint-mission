package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserSummaryResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.enums.Role;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.enums.CommonErrorCode;
import com.sprint.mission.discodeit.exception.enums.UserErrorCode;
import com.sprint.mission.discodeit.exception.user.UserException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.SseService;
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
  private final BinaryContentService binaryContentService;
  private final UserMapper userMapper;

  // PasswordEncoder 주입
  private final PasswordEncoder passwordEncoder;
  private final JwtRegistry jwtRegistry;

  private final ApplicationEventPublisher eventPublisher;
  private final SseService sseService;

  @Override
  @CacheEvict(value = "users", allEntries = true)
  @Transactional
  public UserResponse create(UserCreateRequest request) {
    validateCreateRequest(request);
    log.info("회원가입 요청: username={}, email={}", request.username(), request.email());

    if (userRepository.findByUsername(request.username()).isPresent()) {
      throw new UserException(UserErrorCode.DUPLICATE_USERNAME);
    }
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
    }

    // 평문 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.password());

    User user = new User(
        request.username(),
        request.email(),
        encodedPassword
    );

    if (request.profile() != null) {
      BinaryContent profile = binaryContentService.create(request.profile());
      user.updateProfile(profile);
    }

    User savedUser = userRepository.save(user);

    log.info("회원가입 완료: userId={}", savedUser.getId());

    UserResponse response = userMapper.toUserResponse(savedUser, isOnline(savedUser.getId()));
    sseService.broadcast("users.created", response);

    return response;
  }

  @Override
  @Cacheable(value = "users")
  public List<UserResponse> findAll() {
    List<User> users = userRepository.findAllWithProfile();
    log.debug("전체 유저 조회 요청");

    return users.stream()
        .map(user ->
            userMapper.toUserResponse(user, isOnline(user.getId()))
        )
        .toList();
  }

  @Override
  @Cacheable(value = "users")
  public List<UserSummaryResponse> findAllUserSummaries() {
    List<User> users = userRepository.findAllWithProfile();
    log.debug("전체 유저 요약 조회 요청");

    return users.stream()
        .map(user ->
            userMapper.toUserSummaryResponse(user, isOnline(user.getId()))
        )
        .toList();
  }

  @Override
  @Transactional
  // 사용자 정보 수정은 본인만 가능
  @PreAuthorize("@userSecurity.isOwner(authentication, #userId)")
  @CacheEvict(value = "users", allEntries = true)
  public UserResponse update(UUID userId, UserUpdateRequest request) {
    validateUpdateRequest(userId, request);
    log.info("유저 정보 수정 요청 : userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    if (request.newUsername() != null) {
      userRepository.findByUsername(request.newUsername())
          .filter(found -> !found.getId().equals(user.getId()))
          .ifPresent(found -> {
            throw new UserException(UserErrorCode.DUPLICATE_USERNAME);
          });
      user.updateUsername(request.newUsername());
    }
    if (request.newEmail() != null) {
      userRepository.findByEmail(request.newEmail())
          .filter(found -> !found.getId().equals(user.getId()))
          .ifPresent(found -> {
            throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
          });
      user.updateEmail(request.newEmail());
    }
    if (request.newPassword() != null) {
      user.updatePassword(request.newPassword());
    }

    if (request.newProfile() != null) {
      BinaryContent newProfile = binaryContentService.create(request.newProfile());
      user.updateProfile(newProfile);
    }

    User updated = userRepository.save(user);

    log.info("유저 정보 수정 완료 : userId={}", updated.getId());

    UserResponse response = userMapper.toUserResponse(updated, isOnline(userId));
    sseService.broadcast("users.updated", response);

    return response;
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  @CacheEvict(value = "users", allEntries = true)
  @Transactional
  public UserResponse updateUserRole(UserRoleUpdateRequest request) {

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    Role oldRole = user.getRole();
    user.updateRole(request.newRole());

    eventPublisher.publishEvent(new RoleUpdatedEvent(
        user.getId(),
        oldRole,
        request.newRole()
    ));

    // Jwt Registry에서 해당 유저 토큰 전체 무효화 (강제 로그아웃 처리)
    jwtRegistry.invalidateJwtInformationByUserId(request.userId());

    UserResponse response = userMapper.toUserResponse(user, isOnline(request.userId()));
    sseService.broadcast("users.updated", response);

    return response;
  }

  @Override
  @Transactional
  // 사용자 정보 삭제는 본인만 가능
  @PreAuthorize("@userSecurity.isOwner(authentication, #userId)")
  @CacheEvict(value = "users", allEntries = true)
  public void deleteById(UUID userId) {
    if (userId == null) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "userId는 필수입니다.");
    }
    log.info("유저 삭제 요청 : userId={}", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    UserResponse response = userMapper.toUserResponse(user, false);

    userRepository.deleteById(userId);
    sseService.broadcast("users.deleted", response);
  }

  private void validateCreateRequest(UserCreateRequest request) {
    if (request == null) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "요청이 null입니다.");
    }
    if (request.username() == null || request.username().isBlank()) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "사용자명은 필수입니다.");
    }
    if (request.password() == null || request.password().isBlank()) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "비밀번호는 필수입니다.");
    }
    if (request.email() == null || request.email().isBlank()) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "이메일은 필수입니다.");
    }
  }

  private void validateUpdateRequest(UUID userId, UserUpdateRequest request) {
    if (request == null) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "요청이 null입니다.");
    }
    if (userId == null) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "userId는 필수입니다.");
    }

    boolean hasAnyUpdate =
        request.newUsername() != null ||
            request.newPassword() != null ||
            request.newEmail() != null ||
            request.newProfile() != null;

    if (!hasAnyUpdate) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "수정할 정보가 없습니다.");
    }
  }

  private boolean isOnline(UUID userId) {
    return jwtRegistry.hasActiveJwtInformationByUserId(userId);
  }
}
