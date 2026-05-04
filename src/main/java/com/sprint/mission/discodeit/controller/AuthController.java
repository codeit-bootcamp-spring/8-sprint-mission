package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.dto.JwtDTO;
import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.store.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService userDetailsService;
    private final JwtRegistry jwtRegistry;

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

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(
                    name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                    required = false
            )
            String refreshToken,
            HttpServletResponse response) {
        try {
            JwtInformation information = authService.refreshToken(refreshToken, response);
            return ResponseEntity.ok(new JwtDTO(information.getUserDto(), information.getAccessToken()));
        } catch (DiscodeitException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/role")
    public ResponseEntity<UserDto> updateUserRole(@RequestBody RoleUpdateRequest roleUpdateRequest) {
        log.info("[AuthController] 사용자 권한 변경 요청 접수됨...");
        try {
            UserDto userDto = authService.updateRole(roleUpdateRequest);

            log.info("[AuthController] 권한 변경 성공! - {}", userDto);
            return ResponseEntity.ok(userDto);
        } catch (IllegalArgumentException e) {
            log.error("[AuthController] - 권한 변경 실패! - {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
