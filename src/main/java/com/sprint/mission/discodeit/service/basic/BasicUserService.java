package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
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

    @Override
    public UserResponse create(String name, String email, String password, String profileImage) {
        // 1. 유저 엔티티 생성 및 저장 (프로필 이미지 반영)
        User user = new User(name, email, password, profileImage);
        userRepository.save(user);

        // 2. 유저 상태 정보 즉시 초기화 (로그인 시 Null 방지)
        UserStatus status = new UserStatus(user.getId());
        userStatusRepository.save(status);

        return convertToResponse(user);
    }

    @Override
    public UserResponse login(String email, String password) {
        // 1. 이메일로 유저 조회 (NullPointerException 방지를 위해 email null 체크 추가)
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 2. [멘토 피드백 반영] 비밀번호 직접 검증 (User 엔티티의 Getter 활용)
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. [멘토 피드백 반영] 온라인 상태 업데이트 (접속 시간 갱신)
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseGet(() -> userStatusRepository.save(new UserStatus(user.getId())));

        status.updateLastAccessAt(); // 접속 시간 갱신 로직 실행
        userStatusRepository.save(status);

        return convertToResponse(user);
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
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        return convertToResponse(user);
    }

    @Override
    public UserResponse update(UUID id, String name, String password, String profileImage) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // [멘토 피드백 반영] 선택적 프로필 이미지 교체 기능
        user.update(name, password, profileImage);
        userRepository.save(user);

        return convertToResponse(user);
    }

    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }

    /**
     * Entity -> UserResponse 변환 (7개 파라미터 규격 준수 및 Null 안정성 확보)
     */
    private UserResponse convertToResponse(User user) {
        // 유저와 연결된 UserStatus 조회
        UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);

        return new UserResponse(
                user.getId(),                           // 1. id (UUID)
                user.getName(),                         // 2. name (String)
                user.getEmail(),                        // 3. email (String)
                status != null ? status.getId() : null, // 4. statusId (UUID)
                user.getId(),                           // 5. userId (UUID)
                status != null && status.isOnline(),    // 6. 온라인 여부 (boolean)
                user.getProfileImage()                  // 7. 프로필 이미지 (String)
        );
    }
}