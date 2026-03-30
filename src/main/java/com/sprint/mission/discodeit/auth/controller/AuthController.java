package com.sprint.mission.discodeit.auth.controller;

import com.sprint.mission.discodeit.auth.constants.AuthConstants;
import com.sprint.mission.discodeit.auth.dto.AuthSuccessResponse;
import com.sprint.mission.discodeit.auth.dto.CsrfTokenResponse;
import com.sprint.mission.discodeit.auth.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.service.UserService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

  private final UserMapper userMapper;
  private final DiscodeitUserDetailsService userDetailsService;
  private final UserService userService;


  @GetMapping("/csrf-token")
  public ResponseEntity<AuthSuccessResponse<CsrfTokenResponse>> getCsrfToken(CsrfToken csrfToken) {
    CsrfTokenResponse tokenResponse = new CsrfTokenResponse(csrfToken.getToken(),
        csrfToken.getHeaderName(), csrfToken.getParameterName());
    AuthSuccessResponse<CsrfTokenResponse> response = new AuthSuccessResponse<>(tokenResponse);

    log.info("{} CSRF 토큰 발급 완료", AuthConstants.LOG_PREFIX_CSRF_TOKEN);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<AuthSuccessResponse<UserDto>> getUserDetails(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {

    if (userDetails == null) {
      log.warn("{} 인증되지 않은 사용자가 /me 엔드포인트 접근", AuthConstants.LOG_PREFIX_CSRF_TOKEN);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UserDto userDto = userMapper.toDto(userDetails.getUser());
    AuthSuccessResponse<UserDto> response = new AuthSuccessResponse<>(userDto);

    log.info("{} 사용자 정보 조회: {}", AuthConstants.LOG_PREFIX_CSRF_TOKEN, userDto.email());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/role")
  public ResponseEntity<UserDto> updateRole(@RequestBody UserRoleUpdateRequest request) {

    UserDto updatedUser = userService.updateRole(request.userId(), request.newRole());

    // 권한 변경 이후 최신 정보 재로딩
    userDetailsService.loadUserByUsername(updatedUser.username());

    log.info("{} 사용자 Role 변경 완료: {}", AuthConstants.LOG_PREFIX_CSRF_TOKEN, updatedUser.email());
    return ResponseEntity.ok(updatedUser);
  }
}
