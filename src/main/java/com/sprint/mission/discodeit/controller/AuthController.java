package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.AuthApi;
import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final UserService userService;
  private final AuthService authService;

  // CSRF 토큰 발급 API
  @Override
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    // 로직상 이미 SpaCsrfTokenRequestHandler에서 토큰을 생성(get)했으므로
    // 추가적인 getToken() 호출이 없어도 쿠키가 정상적으로 나갈 수 있기에
    // 따로 출력되는 토큰 log는 추가 X
    log.info("CSRF 토큰 발급 API 호출됨");
    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        .build();
  }

  // 권한 변경
  @Override
  public ResponseEntity<UserDto> updateUserRole(@RequestBody UserRoleUpdateRequest request) {
    UserDto updatedUser = userService.updateRole(request.userId(), request.newRole());
    return ResponseEntity.ok(updatedUser);
  }

  // 리프레시 토큰 재발급
  @Override
  public ResponseEntity<JwtDto> refresh(@CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken, HttpServletResponse response) {

    JwtDto jwtDto = authService.refresh(refreshToken, response);

    return ResponseEntity.ok(jwtDto);
  }
}
