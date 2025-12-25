package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse login(LoginRequest request) {
        // 1. Repository의 findByEmail을 사용하여 유저 조회 (조회 위임)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 2.  비밀번호 검증 로직 추가 (필수 요구사항)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 로그인 성공 시 온라인 상태로 변경
        userStatusRepository.findByUserId(user.getId())
                .ifPresent(status -> {
                    // UserStatus 엔티티에 상태 변경 메서드가 있다고 가정 (예: updateStatus)
                    UserStatus updatedStatus = new UserStatus(user.getId(), true);
                    userStatusRepository.save(updatedStatus);
                });

        return convertToResponse(user);
    }

    private UserResponse convertToResponse(User user) {
        // 기존 코드와 동일하게 UserResponse 빌더 호출
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileId(user.getProfileId())
                .isOnline(true)
                .build();
    }
}