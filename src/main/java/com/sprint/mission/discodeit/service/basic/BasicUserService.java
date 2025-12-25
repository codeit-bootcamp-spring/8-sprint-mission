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
        // 1. 가입 시 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 2. 가입 시 사용자 이름(Unique) 중복 검사
        if (userRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
        }

        UUID profileId = null;
        if (request.getFileName() != null) {
            BinaryContent profile = new BinaryContent(request.getFileName(), request.getFileType(), request.getFileSize());
            profileId = binaryContentRepository.save(profile).getId();
        }

        // User 엔티티 생성 (비밀번호 포함)
        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                profileId
        );
        User savedUser = userRepository.save(user);

        // 유저 상태 초기화 (Offline)
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
        // 1. 수정 대상 유저 확인
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 2. 이메일 변경 시 중복 검사 (본인 제외)
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            }
        }

        // 3. 이름 변경 시 중복 검사 (본인 제외)
        if (!user.getName().equals(request.getName())) {
            if (userRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
            }
        }

        // 4. 정보 업데이트
        user.update(request.getName(), request.getEmail());
        User updated = userRepository.save(user);

        return convertToResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 연관 데이터 삭제 (Status)
        userStatusRepository.findByUserId(id)
                .ifPresent(status -> userStatusRepository.delete(status.getId()));

        // 연관 데이터 삭제 (Profile Binary)
        if (user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }

        // 유저 삭제
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