package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserService userService;

    @Override
    public UserResponse login(String email, String password) {
        // 인증과 관련된 비즈니스 로직을 UserService의 login에 위임
        // 여기서 비밀번호 검증 및 상태 업데이트가 한 번에 이루어집니다.
        return userService.login(email, password);
    }

    @Override
    public UserResponse login(LoginRequest request) {
        return null;
    }
}