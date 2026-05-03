package com.sprint.mission.discodeit.api;

import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserRoleUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

  @Operation(summary = "CSRF 토큰 발급")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "203",
          description = "토큰 생성 성공 (응답 헤더의 Set-Cookie 확인)"
      )
  })
  @GetMapping("/csrf-token")
  ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken);

  @Operation(summary = "사용자 권한 수정")
  @PutMapping("/role")
  ResponseEntity<UserDto> updateUserRole(@RequestBody UserRoleUpdateRequest request);

  @Operation(summary = "리프레시 토큰 재발급")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
  })
  @PostMapping("/refresh")
  ResponseEntity<JwtDto> refresh(
      @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken, HttpServletResponse response
  );
}
