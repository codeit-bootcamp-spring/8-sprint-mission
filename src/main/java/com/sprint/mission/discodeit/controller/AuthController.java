package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.AuthApi;
import com.sprint.mission.discodeit.dto.user.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final AuthService authService;

  // 로그인
  @Override
  public ResponseEntity<UserDto> login(LoginRequest request) {
    UserDto user = authService.login(request);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(user);
  }
}
