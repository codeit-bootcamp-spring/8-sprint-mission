package com.sprint.mission.discodeit.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;

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
}
