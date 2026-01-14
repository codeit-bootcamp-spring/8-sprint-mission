package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.UserDto;
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
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {

  private final UserRepository userRepository;

  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public User create(UserCreateRequest request,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    if (userRepository.existsByName(request.username())) {
      throw new IllegalArgumentException(
          "User with name " + request.username() + " already exists");
    }
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("User with email " + request.email() + " already exists.");
    }

    UUID profileIdNullable = optionalProfileCreateRequest
        .map(profileRequest -> {
          String fileName = profileRequest.fileName();
          String contentType = profileRequest.contentType();
          byte[] bytes = profileRequest.bytes();
          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType, bytes);
          return binaryContentRepository.save(binaryContent).getId();
        })
        .orElse(null);

    String encodedPassword = passwordEncoder.encode(request.password());

    User user = new User(request.username(), request.email(), encodedPassword, profileIdNullable);

    User savedUser = userRepository.save(user);

    Instant now = Instant.now();
    UserStatus status = new UserStatus(savedUser.getId(), now);
    userStatusRepository.save(status);

    return savedUser;
  }

  @Override
  public UserDto find(UUID userId) {
    return userRepository.findById(userId)
        .map(this::toDto)
        .orElseThrow(() -> new NoSuchElementException(userId + "에 해당하는 유저가 없습니다."));
  }

  @Override
  public List<UserDto> findAll() {
    List<User> users = userRepository.findAll();

    return users.stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  public User update(UUID userId, UserUpdateRequest request,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("수정하려는 유저가 없습니다."));

    // 기존 유저의 이름과 요청한 이름이 다를 경우, 같으면 넘어감
    if (!user.getUsername().equals(request.newUsername())) {
      if (userRepository.existsByName(request.newUsername())) {
        throw new IllegalArgumentException("이미 " + request.newUsername() + "을 가진 유저가 존재합니다.");
      }
    }

    // 기존 유저의 이메일과 요청한 이메일 다를 경우, 같으면 넘어감
    if (!user.getEmail().equals(request.newEmail())) {
      if (userRepository.existsByEmail(request.newEmail())) {
        throw new IllegalArgumentException("이미 " + request.newEmail() + "을 가진 유저가 존재합니다.");
      }
    }

    UUID nullableProfileId = optionalProfileCreateRequest
        .map(profileRequest -> {
          Optional.ofNullable(user.getProfileId())
              .ifPresent(binaryContentRepository::deleteById);

          String fileName = profileRequest.fileName();
          String contentType = profileRequest.contentType();
          byte[] bytes = profileRequest.bytes();
          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType, bytes);
          return binaryContentRepository.save(binaryContent).getId();
        })
        .orElse(null);

    String encodedPassword = user.getPassword();
    if (request.newPassword() != null) {
      encodedPassword = passwordEncoder.encode(request.newPassword());
    }

    user.update(request.newUsername(), request.newEmail(), encodedPassword, nullableProfileId);

    return userRepository.save(user);
  }

  @Override
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("삭제하려는 유저가 없습니다."));

    userStatusRepository.deleteByUserId(user.getId());

    if (user.getProfileId() != null) {
      binaryContentRepository.deleteById(user.getProfileId());
    }

    userRepository.delete(userId);
  }

  private UserDto toDto(User user) {
    Boolean online = userStatusRepository.findByUserId(user.getId())
        .map(UserStatus::isOnline)
        .orElse(null);

    return new UserDto(
        user.getId(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getUsername(),
        user.getEmail(),
        user.getProfileId(),
        online
    );
  }
}
