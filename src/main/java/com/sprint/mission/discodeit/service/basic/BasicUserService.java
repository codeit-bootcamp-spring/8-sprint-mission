package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveFailedException;
import com.sprint.mission.discodeit.exception.user.UserEmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UsernameAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  @Override
  public UserDto create(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> profileCreateRequest) {
    log.info("[UserService] 사용자 생성 시작 - 이름: {}", userCreateRequest.username());

    String username = userCreateRequest.username();
    String email = userCreateRequest.email();
    String password = userCreateRequest.password();

    if (userRepository.existsByUsername(username)) { // username 중복 확인
      log.warn("[UserService] 사용자 생성 실패 - 중복된 이름: {}", username);
      throw new UsernameAlreadyExistsException(username);
    }

    if (userRepository.existsByEmail(email)) {
      log.warn("[UserService] 사용자 생성 실패 - 중복된 이메일: {}", email);
      throw new UserEmailAlreadyExistsException(email);
    }

    BinaryContent profile = profileCreateRequest
        .map(profileRequest -> {
          log.debug("[UserService] 사용자 프로필 이미지 업로드 시작 - 이름: {}, 크기: {} bytes",
              profileRequest.fileName(), profileRequest.bytes().length);
          String fileName = profileRequest.fileName();
          String contentType = profileRequest.contentType();
          byte[] bytes = profileRequest.bytes();
          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType);
          binaryContentRepository.save(binaryContent);

          try {
            binaryContentStorage.put(binaryContent.getId(), bytes);
          } catch (Exception e) {
            log.error("[UserService] 프로필 이미지 스토리지 저장 실패 - 파일명: {}, 원인: {}",
                profileRequest.fileName(), e.getMessage());
            throw new BinaryContentSaveFailedException(binaryContent.getId(), fileName);
          }
          return binaryContent;
        })
        .orElse(null);

    String encryptedPassword = passwordEncoder.encode(password);

    User user = new User(username, email, encryptedPassword, profile);

    userRepository.save(user);

    log.info("[UserService] 사용자 생성 완료 - ID: {},  프로필 여부: {}", user.getId(), (profile != null));
    return userMapper.toDto(user);
  }

  @Override
  public UserDto find(UUID userId) {
    log.debug("[UserService] 사용자 조회 시작 - Id: {}", userId);

    return userRepository.findById(userId)
        .map(userMapper::toDto)
        .orElseThrow(() -> {
          log.warn("[UserService] 사용자 조회 실패- 존재하지 않는 Id: {}", userId);
          return new UserNotFoundException(userId);
        });
  }

  @Transactional
  @Override
  public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest,
      Optional<BinaryContentCreateRequest> profileCreateRequest) {
    log.info("[UserService] 사용자 수정 시작 - Id: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("[UserService] 사용자 수정 실패 - 존재하지 않는 Id: {}", userId);
          return new UserNotFoundException(userId);
        });

    String newUsername = userUpdateRequest.newUsername();
    String newEmail = userUpdateRequest.newEmail();
    String newPassword = userUpdateRequest.newPassword();

    if (userRepository.existsByUsername(newUsername)) { // username 중복 확인
      log.warn("[UserService] 사용자 수정 실패 - 중복된 이름: {}", newUsername);
      throw new UsernameAlreadyExistsException(newUsername);
    }

    if (userRepository.existsByEmail(newEmail)) {
      log.warn("[UserService] 사용자 수정 실패 - 중복된 이메일: {}", newEmail);
      throw new UserEmailAlreadyExistsException(newEmail);
    }

    BinaryContent newProfile = null;

    if (profileCreateRequest.isPresent()) {
      BinaryContentCreateRequest profileRequest = profileCreateRequest.get();

      log.debug("[UserService] 사용자 새 프로필 이미지 업로드 시작 - 이름: {}, 크기: {}", profileRequest.fileName(),
          profileRequest.bytes().length);
      byte[] bytes = profileRequest.bytes();
      newProfile = new BinaryContent(
          profileRequest.fileName(),
          (long) bytes.length,
          profileRequest.contentType()
      );

      binaryContentRepository.save(newProfile);

      try {
        binaryContentStorage.put(newProfile.getId(), bytes);
      } catch (Exception e) {
        log.error("[UserService] 프로필 이미지 스토리지 저장 실패 - 유저Id: {}, 원인: {}",
            userId, e.getMessage());
        throw new BinaryContentSaveFailedException(newProfile.getId(), newProfile.getFileName());
      }
    }

    user.update(newUsername, newEmail, newPassword, newProfile);

    log.info("[UserService] 사용자 수정 완료 - ID: {}", userId);
    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public void delete(UUID userId) {
    log.info("[UserService] 사용자 삭제 시작 - ID: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("[UserService] 사용자 삭제 실패 - 존재하지 않는 Id: {}", userId);
          return new UserNotFoundException(userId);
        });

    userRepository.deleteById(userId);
    log.info("[UserService] 사용자 삭제 완료 - ID: {}", user.getId());
  }

  @Override
  public List<UserDto> findAll() {
    log.debug("[UserService] 전체 사용자 목록 조회 시작");

    List<UserDto> result = userRepository.findAllWithProfile().stream()
        .map(userMapper::toDto)
        .toList();

    log.info("[UserService] 전체 사용자 목록 조회 완료 - 총 인원: {}명", result.size());
    return result;
  }
}
