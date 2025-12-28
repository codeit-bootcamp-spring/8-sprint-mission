package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    //  멘토님 피드백 반영: 로그인 시 비밀번호를 비교하고 프로필 이미지와 상태 정보가 포함된 UserResponse 반환
    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }
}