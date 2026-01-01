package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.request.LoginRequest;
import com.sprint.mission.discodeit.DTO.request.LogoutRequest;
import com.sprint.mission.discodeit.DTO.response.LoginResponse;
import com.sprint.mission.discodeit.DTO.response.LogoutResponse;
import com.sprint.mission.discodeit.DTO.response.UserResponse;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class LoginController {
    private final AuthService authService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
        UserResponse userResponse = authService.login(request);
        return ResponseEntity.ok(
                new LoginResponse(
                        userResponse.name() + " 님이 로그인했습니다.",
                        userResponse
                )
        );
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ResponseEntity<LogoutResponse> logout(@RequestBody LogoutRequest request) {
        String username = authService.logout(request.id());
        return ResponseEntity.ok(
                new LogoutResponse(username + " 님이 로그아웃했습니다."));
    }

}
