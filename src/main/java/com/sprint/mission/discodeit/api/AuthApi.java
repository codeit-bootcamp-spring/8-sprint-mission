package com.sprint.mission.discodeit.api;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  @Operation(
      summary = "현재 로그인 사용자 정보 조회",
      description = "세션 정보를 바탕으로 현재 로그인된 사용자의 상세 정보를 반환한다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "조회 성공"
      ),
      @ApiResponse(
          responseCode = "401",
          description = "인증되지 않은 사용자 (로그인 필요)"
      )
  })
  @GetMapping("/me")
  ResponseEntity<UserDto> getMe(@AuthenticationPrincipal DiscodeitUserDetails userDetails);
}
