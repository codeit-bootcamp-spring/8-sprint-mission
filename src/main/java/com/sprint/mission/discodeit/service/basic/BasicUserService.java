package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    if (userRepository.existsByUsername(username)) {
      throw new IllegalArgumentException(
          "User with name " + request.username() + " 이미 존재하는 유저 이름입니다.");
    }
    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException(
          "User with email " + request.email() + " 이미 존재하는 이메일 입니다.");
    }

    BinaryContent profile = optionalProfileCreateRequest
        .map(this::saveBinaryContent)
        .orElse(null);

    if (profile != null) {
      binaryContentStorage.put(profile.getId(), optionalProfileCreateRequest.get().bytes());
    }

    String encodedPassword = passwordEncoder.encode(request.password());

    User user = new User(username, email, encodedPassword, profile);
    User savedUser = userRepository.save(user);

    Instant now = Instant.now();
    UserStatus status = new UserStatus(savedUser, now);

    userStatusRepository.save(status);

    return userMapper.toDto(savedUser);
  }

  @Override
  public UserDto find(UUID userId) {
    return userRepository.findById(userId)
        .map(userMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException(userId + " 해당하는 유저가 없습니다."));
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

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException(userId + " 해당하는 유저가 존재하지 않습니다."));

    // 기존 유저의 이름과 요청한 이름이 다를 경우, 같으면 넘어감
    if (!user.getUsername().equals(newUsername)) {
      if (userRepository.existsByUsername(newUsername)) {
        throw new IllegalArgumentException("이미 " + newUsername + " 해당하는 유저가 존재합니다.");
      }
    }

    // 기존 유저의 이메일과 요청한 이메일 다를 경우, 같으면 넘어감
    if (!user.getEmail().equals(newEmail)) {
      if (userRepository.existsByEmail(newEmail)) {
        throw new IllegalArgumentException("이미 " + newEmail + " 해당하는 유저가 존재합니다.");
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
    }

    String encodedPassword = user.getPassword();
    if (request.newPassword() != null) {
      encodedPassword = passwordEncoder.encode(request.newPassword());
    }

    user.update(request.newUsername(), request.newEmail(), encodedPassword, newProfile);

    return userMapper.toDto(userRepository.save(user));
  }

  @Override
  @Transactional
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException(userId + " 해당하는 유저가 없습니다."));

    if (user.getProfile() != null) {
      binaryContentRepository.deleteById(user.getProfile().getId());
    }

    userRepository.delete(user);
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
