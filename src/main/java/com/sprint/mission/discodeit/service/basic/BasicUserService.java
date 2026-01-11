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
        // 1. 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 2. BinaryContent 저장 후 ID 획득
        BinaryContent profileContent = new BinaryContent();
        BinaryContent savedContent = binaryContentRepository.save(profileContent);

        // 3. User 객체 생성 및 저장 (반드시 저장된 객체를 변수에 담으세요)
        User user = new User(
                request.getUsername() != null ? request.getUsername() : request.getName(),
                request.getEmail(),
                request.getPassword(),
                savedContent.getId()
        );
        User savedUser = userRepository.save(user); //  중요: 리포지토리가 반환하는 객체 사용

        // 4. 상태 저장
        userStatusRepository.save(new UserStatus(savedUser.getId()));

        // 5. 저장된 'savedUser'를 DTO로 변환
        return convertToResponse(savedUser);
    }

    /**
     * 유저 수정: 선택적 프로필 이미지 교체 및 이름 중복 검사
     */
    @Override
    public UserResponse update(UserUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 이름 변경 (name이 제공된 경우)
        String newName = request.getName();
        String newPassword = request.getPassword();
        
        // 이름이 변경되는 경우에만 중복 검사
        if (newName != null && !newName.equals(user.getName())) {
            if (userRepository.existsByName(newName)) {
                throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
            }
        }

        // 3. 업데이트할 필드들을 한 번에 처리
        user.update(newName, null, newPassword);

        // 4. 프로필 이미지 변경은 현재 구현하지 않음 (프론트엔드에서 multipart/form-data로 처리 필요)

        // 5. 사용자 저장
        User savedUser = userRepository.save(user);

        // 6. 응답 DTO 변환
        return convertToResponse(savedUser);
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
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse findById(UUID id) {
        return null;
    }

    @Override
    public UserResponse update(UUID id, String name, String password, String profileImage) {
        return null;
    }

    // ... findAll, findById 등 생략

    @Override
    public void delete(UUID id) {
        userStatusRepository.deleteByUserId(id);
        userRepository.delete(id); // UserRepository 메서드명 확인
    }

    private UserResponse convertToResponse(User user) {
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElse(new UserStatus(user.getId()));

        //  User 엔티티의 Getter를 통해 값을 DTO로 복사
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                status.getId(),
                user.getId(),
                status.isOnline(),
                user.getProfileId()
        );
    }

    // 이 외 메서드들도 convertToResponse(savedUser) 형태로 반환하도록 확인하세요.
}