package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String tokenValue = csrfToken.getToken();
        log.debug("CSRF 토큰 요청: {}", tokenValue);
        String parameterName = csrfToken.getParameterName();
        log.debug("CSRF 파라미터 이름: {}", parameterName);
        String headerName = csrfToken.getHeaderName();
        log.debug("CSRF 헤더 이름: {}", headerName);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/role")
    public ResponseEntity<UserDto> updateUserRole(@RequestBody UserRoleUpdateRequest userRoleUpdateRequest) {
        log.info("[AuthController] 사용자 권한 변경 요청 접수됨...");
        log.info("[AuthController] 요청 데이터: {}", userRoleUpdateRequest);

        // 서비스의 실행 결과에 따른 응답 결정
        try {
            UserDto userDto = userService.updateUserRole(userRoleUpdateRequest.userId(), userRoleUpdateRequest.newRole());
            log.info("[AuthController] 권한 변경 성공! - {}", userDto);

            return ResponseEntity.ok(userDto);
        } catch (IllegalArgumentException e) {
            log.error("[AuthController] - 권한 변경 실패! - {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
