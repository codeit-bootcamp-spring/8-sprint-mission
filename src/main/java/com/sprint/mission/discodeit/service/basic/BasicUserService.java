package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.UserException.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.UserException.DuplicateUsernameException;
import com.sprint.mission.discodeit.exception.UserException.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;

  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserDto create(UserCreateRequest request,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    String username = request.username();
    String email = request.email();

    log.info("Service: 유저 생성 로직 시작 - email: {}, username: {}", username, email);

    if (userRepository.existsByUsername(username)) {
      throw new DuplicateUsernameException(username);
    }
    if (userRepository.existsByEmail(email)) {
      throw new DuplicateEmailException(email);
    }

    BinaryContent profile = optionalProfileCreateRequest
        .map(this::saveBinaryContent)
        .orElse(null);

    if (profile != null) {
      binaryContentStorage.put(profile.getId(), optionalProfileCreateRequest.get().bytes());
      log.debug("Service: 사용자 프로필 이미지 생성 완료 - ID: {}", profile.getId());
    }

    String encodedPassword = passwordEncoder.encode(request.password());

    User user = new User(username, email, encodedPassword, profile);
    User savedUser = userRepository.save(user);

    Instant now = Instant.now();
    UserStatus status = new UserStatus(savedUser, now);

    userStatusRepository.save(status);

    log.info("Service: 유저 생성 완료 및 DB 저장 완료 - ID: {}", savedUser.getId());
    return userMapper.toDto(savedUser);
  }

  @Override
  public UserDto find(UUID userId) {
    return userRepository.findById(userId)
        .map(userMapper::toDto)
        .orElseThrow(() -> new UserNotFoundException(userId));
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
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    String newUsername = request.newUsername();
    String newEmail = request.newEmail();

    log.info("Service: 유저 수정 요청 - ID: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("Service: 유저 수정 실패(존재하지 않는 ID) - ID: {}", userId);
          return new UserNotFoundException(userId);
        });

    // 기존 유저의 이름과 요청한 이름이 다를 경우, 같으면 넘어감
    if (!user.getUsername().equals(newUsername)) {
      if (userRepository.existsByUsername(newUsername)) {
        log.warn("Service: 유저 수정 실패(이미 해당 유저 이름 존재) - username: {}", newUsername);
        throw new DuplicateUsernameException(newUsername);
      }
    }

    // 기존 유저의 이메일과 요청한 이메일 다를 경우, 같으면 넘어감
    if (!user.getEmail().equals(newEmail)) {
      if (userRepository.existsByEmail(newEmail)) {
        log.warn("Service: 유저 수정 실패(이미 해당 유저 이메일 존재) - email: {}", newEmail);
        throw new DuplicateEmailException(newEmail);
      }
    }

    BinaryContent newProfile = user.getProfile();
    if (optionalProfileCreateRequest.isPresent()) {
      if (user.getProfile() != null) {
        UUID userProfileId = user.getProfile().getId();
        //byte를 저장해놓은 기존 파일 삭제
        binaryContentStorage.delete(userProfileId);

        binaryContentRepository.delete(user.getProfile());
      }
      newProfile = saveBinaryContent(optionalProfileCreateRequest.get());
      binaryContentStorage.put(newProfile.getId(), optionalProfileCreateRequest.get().bytes());
      log.debug("Service: 사용자 프로필 수정 완료 - ID: {}", newProfile.getId());
    }

    String encodedPassword = user.getPassword();
    if (request.newPassword() != null) {
      encodedPassword = passwordEncoder.encode(request.newPassword());
      log.debug("Service: 사용자 비밀번호 수정 및 암호화 완료");
    }

    user.update(request.newUsername(), request.newEmail(), encodedPassword, newProfile);

    log.info("Service: 유저 수정 완료 - ID: {}", userId);
    return userMapper.toDto(userRepository.save(user));
  }

  @Override
  @Transactional
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("Service - 사용자 삭제 실패(존재하지 않는 유저) - ID: {}", userId);
          return new UserNotFoundException(userId);
        });

    if (user.getProfile() != null) {
      binaryContentRepository.deleteById(user.getProfile().getId());
      log.debug("Service - 사용자 프로필 이미지 데이터 삭제 완료");
    }

    userRepository.delete(user);
    log.info("Service: 사용자 DB 삭제 완료 - ID: {}", userId);
  }

  //중복 코드 제거
  private BinaryContent saveBinaryContent(BinaryContentCreateRequest request) {
    String fileName = request.fileName();
    String contentType = request.contentType();
    byte[] bytes = request.bytes();
    BinaryContent binaryContent = new BinaryContent(
        fileName,
        (long) bytes.length,
        contentType
    );
    return binaryContentRepository.save(binaryContent);
  }
}
