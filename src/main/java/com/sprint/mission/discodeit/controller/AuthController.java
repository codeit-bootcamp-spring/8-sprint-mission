package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public UserResponse login(@RequestBody LoginRequest loginRequest) {
        // username 필드에 이메일 또는 이름이 올 수 있음
        String usernameOrEmail = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        // LoginRequest의 username 필드를 email 파라미터로 전달
        // (BasicAuthService.login은 이메일 또는 이름 모두 처리 가능)
        return authService.login(usernameOrEmail, password);
    }
}