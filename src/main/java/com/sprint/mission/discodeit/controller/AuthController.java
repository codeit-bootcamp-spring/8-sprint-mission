package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sprint.mission.discodeit.service.AuthService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController implements AuthApi {

  private final AuthService authService;

  @GetMapping("csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);

    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        .build();
  }

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {

    if (userDetails == null) {
      ErrorResponse errorResponse = new ErrorResponse(
          Instant.now(),
          "AUTH_REQUIRED",
          "로그인이 필요한 서비스입니다.",
          null,
          "AuthenticationException",
          HttpServletResponse.SC_UNAUTHORIZED
      );

      return ResponseEntity
          .status(HttpStatus.UNAUTHORIZED)
          .body(errorResponse);
    }

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDetails.getUserDto());
  }

  @PutMapping("/role")
  public ResponseEntity<?> updateUserRole(
      @Valid @RequestBody UserRoleUpdateRequest userRoleUpdateRequest) {

    try {
      UserDto userDto = authService.updateUserRole(userRoleUpdateRequest);
      log.info("[AuthController] 권한 변경 성공: userId={}, newRole={}",
          userRoleUpdateRequest.userId(), userRoleUpdateRequest.newRole());

      return ResponseEntity
          .status(HttpStatus.OK)
          .body(userDto);
    } catch (UserNotFoundException e) {

      log.error("[AuthController] 권한 변경 실패: {}", e.getMessage());

      ErrorResponse errorResponse = new ErrorResponse(
          Instant.now(),
          e.getErrorCode().toString(),
          e.getMessage(),
          null,
          e.getClass().getSimpleName(),
          HttpStatus.NOT_FOUND.value()
      );

      return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(errorResponse);
    }
  }
}
