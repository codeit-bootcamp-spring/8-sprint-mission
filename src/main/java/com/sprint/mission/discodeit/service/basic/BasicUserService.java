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
import com.sprint.mission.discodeit.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    /**
     * 유저 생성: DTO 활용, 중복 검사, BinaryContent 연관 관계 설정
     */
    @Override
    public UserResponse create(UserCreateRequest request) {
        // 1. 유효성 검사 (필수값 및 이메일 형식)
        ValidationUtil.validateNotNullOrEmpty(request.getName(), "이름");
        ValidationUtil.validateNotNullOrEmpty(request.getEmail(), "이메일");
        ValidationUtil.validateEmailFormat(request.getEmail());

        // 2. 중복 검사 (이메일, 이름)
        validateUniqueEmail(request.getEmail());
        validateUniqueName(request.getName());

        // 3. BinaryContent 생성 및 저장 (프로필 이미지 연관 관계)
        BinaryContent binaryContent = new BinaryContent();
        binaryContentRepository.save(binaryContent);

        // 4. 유저 생성 및 저장 (UUID profileId 사용)
        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                binaryContent.getId()
        );
        userRepository.save(user);

        // 5. 유저 상태 정보 동시 생성
        userStatusRepository.save(new UserStatus(user.getId()));

        return convertToResponse(user);
    }

    /**
     * 유저 수정: 선택적 프로필 이미지 교체 및 이름 중복 검사
     */
    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 1. 이름 변경 시 중복 검사
        if (request.getName() != null && !user.getName().equals(request.getName())) {
            validateUniqueName(request.getName());
        }

        // 2. 선택적 프로필 이미지 대체 (새 이미지가 오면 BinaryContent 생성)
        UUID finalProfileId = user.getProfileId();
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            BinaryContent newContent = new BinaryContent();
            binaryContentRepository.save(newContent);
            finalProfileId = newContent.getId();
        }

        user.update(request.getName(), request.getPassword(), finalProfileId);
        userRepository.save(user);

        return convertToResponse(user);
    }

    @Override
    public UserResponse create(String name, String email, String password, String profileImage) {
        return null;
    }

    @Override
    public UserResponse login(String email, String password) {
        return null;
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        return convertToResponse(user);
    }

    @Override
    public UserResponse update(UUID id, String name, String password, String profileImage) {
        return null;
    }

    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }

    /**
     * 중복 검사 헬퍼 메서드
     */
    private void validateUniqueEmail(String email) {
        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.getEmail() != null && u.getEmail().equals(email));
        if (exists) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateUniqueName(String name) {
        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.getName() != null && u.getName().equals(name));
        if (exists) {
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }
    }

    /**
     * Entity -> UserResponse 변환 (7개 파라미터 규격 및 UUID profileId 반영)
     */
    private UserResponse convertToResponse(User user) {
        UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                status != null ? status.getId() : null,
                user.getId(),
                status != null && status.isOnline(),
                user.getProfileId() // UUID 반환
        );
    }
}