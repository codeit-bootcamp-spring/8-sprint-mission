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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(UserCreateRequest request, BinaryContentCreateRequest profileRequest) {

        // Username / Email 중복 검증
        if (userRepository.existsByUsernameOrEmail(request.username(),  request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 username 또는 email 입니다. 다시 입력 부탁드립니다.");
        }

        // User 생성
        User user = new User(request.username(), request.email(), request.password());
        userRepository.save(user);

        // UserStatus 생성 (마지막 접속 시간 = 지금)
        UserStatus status = new UserStatus(user.getId(), Instant.now());
        userStatusRepository.save(status);

        // 프로필 이미지 있으면 BinaryContent 생성 -> User.profileId 설정
        if (profileRequest != null && profileRequest.data() != null) {
            BinaryContent content = new BinaryContent(
                    profileRequest.fileName(),
                    profileRequest.contentType(),
                    profileRequest.data(),
                    user.getId(),
                    null
            );
           binaryContentRepository.save(content);

            user.update(null, null, null, content.getId());
            userRepository.save(user);
        }

        return convertDto(user, status);
    }

    @Override
    public UserResponse findUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다. " + id));

        UserStatus status = userStatusRepository.findByUserId(id)
                .orElse(null);

        return convertDto(user, status);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserStatus status = userStatusRepository.findByUserId(user.getId())
                            .orElse(null);
                    return convertDto(user, status);
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse update(UserUpdateRequest request, BinaryContentCreateRequest profileRequest) {

        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다. " + request.id()));

        // username 변경 시에 중복 체크
        if (request.username() != null && !request.username().equals(user.getName())) {
            userRepository.findByUsername(request.username())
                    .filter(other -> !other.getId().equals(user.getId()))
                    .ifPresent(other -> {
                        throw new IllegalArgumentException("이미 사용 중인 Username 입니다." + request.username());
                    });
        }

        // email 변경 시에 중복 체크
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            userRepository.findByEmail(request.email())
                    .filter(other -> !other.getId().equals(user.getId()))
                    .ifPresent(other -> {
                        throw new IllegalArgumentException("이미 사용 중인 Email 입니다." + request.email());
                    });
                    }

        // 프로필 이미지 교체
        UUID newProfileId = user.getProfileImageId();

        if (profileRequest != null && profileRequest.data() != null) {
            // 기존 이미지 삭제 (존재 한다면)
            if (user.getProfileImageId() != null) {
                binaryContentRepository.deleteById(user.getProfileImageId());
            }

            // 새 이미지 저장
            BinaryContent content = new BinaryContent(
                    profileRequest.fileName(),
                    profileRequest.contentType(),
                    profileRequest.data(),
                    user.getId(),
                    null
            );
            binaryContentRepository.save(content);
            newProfileId = content.getId();
        }

        // 유저 정보 업데이트
        user.update(request.username(), request.email(), request.password(), newProfileId);

        // 덮어쓰기
        userRepository.save(user);

        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElse(null);

        return convertDto(user, status);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다: " + id));

        // 프로필 이미지 삭제
        if (user.getProfileImageId() != null) {
            binaryContentRepository.deleteById(user.getProfileImageId());
        }

        // UserStatus 삭제
        userStatusRepository.findByUserId(id)
                .ifPresent(status -> userStatusRepository.deleteById(status.getId()));

        // User 삭제
        userRepository.delete(id);
    }

    private UserResponse convertDto(User user, UserStatus status) {
        boolean online = false;
        Instant lastConn = null;

        if (status != null) {
            online = status.isOnline();
            lastConn = status.getLastConnAt();
        }

        return new UserResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getName(),
                user.getEmail(),
                online,
                lastConn,
                user.getProfileImageId()
        );
    }
}
