package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        UUID profileId = null;
        if (request.getFileName() != null) {
            BinaryContent profile = new BinaryContent(request.getFileName(), request.getFileType(), request.getFileSize());
            profileId = binaryContentRepository.save(profile).getId();
        }

        // 수정된 User 엔티티 생성자 호출 (String, String, UUID)
        User user = new User(request.getName(), request.getEmail(), profileId);
        User savedUser = userRepository.save(user);

        // 수정된 UserStatus 엔티티 생성자 호출 (UUID, boolean)
        UserStatus status = new UserStatus(savedUser.getId(), false);
        userStatusRepository.save(status);

        return convertToResponse(savedUser);
    }

    @Override
    public Optional<UserResponse> findById(UUID id) {
        return userRepository.findById(id).map(this::convertToResponse);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 엔티티의 update(String, String) 호출
        user.update(request.getName(), request.getEmail());
        User updated = userRepository.save(user);
        return convertToResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 1. 유저 상태 정보 삭제
        userStatusRepository.findByUserId(id)
                .ifPresent(status -> userStatusRepository.delete(status.getId()));

        // 2. 프로필 이미지 정보 삭제 (있는 경우)
        if (user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }

        // 3. 유저 본인 삭제
        userRepository.delete(id);
    }

    private UserResponse convertToResponse(User user) {
        boolean isOnline = userStatusRepository.findByUserId(user.getId())
                .map(UserStatus::isOnline)
                .orElse(false);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileId(user.getProfileId())
                .isOnline(isOnline)
                .build();
    }
}