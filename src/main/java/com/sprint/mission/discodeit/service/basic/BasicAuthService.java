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

    // [멘토님 피드백 반영] UserRepository와 UserStatusRepository를 직접 DI
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse login(String email, String password) {
        // 1. [인증] 이메일 또는 사용자명으로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 이메일로 찾지 못하면 이름으로 시도 (프론트엔드 호환성)
                    return userRepository.findAll().stream()
                            .filter(u -> u.getName() != null && u.getName().equals(email))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 또는 사용자명입니다."));
                });

        // 2. [인증] 비밀번호 검증 (UserService에 위임하지 않고 직접 수행)
        if (user.getPassword() == null || !user.getPassword().equals(password)) {
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
        // username 필드에 이메일 또는 사용자명이 올 수 있음
        String usernameOrEmail = request.getUsername();
        String password = request.getPassword();
        
        if (usernameOrEmail == null || usernameOrEmail.isEmpty()) {
            throw new IllegalArgumentException("사용자명 또는 이메일을 입력해주세요.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
        
        // 기존 login(String email, String password) 메서드 재사용
        // (이메일 또는 이름 모두 처리 가능)
        return login(usernameOrEmail, password);
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