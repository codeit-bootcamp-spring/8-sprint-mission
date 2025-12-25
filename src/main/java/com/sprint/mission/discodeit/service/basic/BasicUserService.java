package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
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

    @Override
    public UserResponse create(UserCreateRequest request) {
        User user = new User(request.getName(), request.getEmail(), request.getPassword());
        User savedUser = userRepository.save(user);

        UserStatus status = new UserStatus(savedUser.getId());
        userStatusRepository.save(status);

        return convertToResponse(savedUser, status);
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 멘토님 피드백 반영: 프로필 이미지를 포함한 업데이트 로직
        // 실제 파일 시스템 연동 시 여기서 기존 파일 삭제 로직이 들어갈 수 있습니다.
        user.update(request.getName(), request.getPassword(), request.getProfileImage());

        userRepository.save(user);

        UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
        return convertToResponse(user, status);
    }

    @Override
    public Optional<UserResponse> findById(UUID id) {
        return userRepository.findById(id).map(user -> {
            UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
            return convertToResponse(user, status);
        });
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
                    return convertToResponse(user, status);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        userStatusRepository.findByUserId(id).ifPresent(s -> userStatusRepository.delete(s.getId()));
        userRepository.delete(id);
    }

    private UserResponse convertToResponse(User user, UserStatus status) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .isOnline(status != null && status.isOnline())
                .profileImage(user.getProfileImage()) //  Response에도 추가 확인
                .build();
    }
}