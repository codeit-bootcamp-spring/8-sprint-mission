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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    // [멘토님 피드백 반영] UserRepository와 UserStatusRepository를 직접 DI
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse login(String email, String password) {
        // 1. [인증] 이메일로 사용자 조회
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 2. [인증] 비밀번호 검증 (UserService에 위임하지 않고 직접 수행)
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. [상태 관리] 온라인 상태 업데이트
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseGet(() -> userStatusRepository.save(new UserStatus(user.getId())));

        status.updateLastAccessAt();
        userStatusRepository.save(status);

        // 4. 응답 DTO 변환 및 반환
        return convertToResponse(user, status);
    }

    @Override
    public UserResponse login(LoginRequest request) {
        return null;
    }

    /**
     * Entity -> UserResponse 변환 (AuthService 전용 private 메서드)
     */
    private UserResponse convertToResponse(User user, UserStatus status) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                status != null ? status.getId() : null,
                user.getId(),
                status != null && status.isOnline(),
                user.getProfileId()
        );
    }
}