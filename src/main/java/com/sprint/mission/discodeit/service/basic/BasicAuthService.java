package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository; // 인터페이스 참조
import com.sprint.mission.discodeit.repository.UserStatusRepository; // 인터페이스 참조
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    // 특정 구현체(JCF...)가 아닌 인터페이스를 주입받습니다.
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse login(LoginRequest request) {
        // 현재 레포지토리에 저장된 모든 유저를 가져옵니다.
        List<User> allUsers = userRepository.findAll();

        // 디버깅을 위해 현재 로드된 유저 수를 출력합니다.
        System.out.println("[DEBUG] 로그인 시도 - 현재 레포지토리 유저 수: " + allUsers.size());

        User user = allUsers.stream()
                .filter(u -> u.getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("로그인 실패: [" + request.getEmail() + "] 유저를 찾을 수 없습니다."));

        // 유저 상태 정보 조회
        UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileId(user.getProfileId())
                .isOnline(status != null && status.isOnline())
                .build();
    }
}