package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.response.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final DiscodeitUserDetailsService userDetailsService;

    @GetMapping(path = "csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String tokenValue = csrfToken.getToken();
        log.debug("CSRF 토큰 요청: {}", tokenValue);
        return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
    }

    @PostMapping(path = "refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(value = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken) || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse(java.time.Instant.now(), "UNAUTHORIZED", "Invalid or expired refresh token", null, null, 401)
            );
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtTokenProvider.createAccessToken(username);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(username);

        java.time.Instant newAccessExpiry = jwtTokenProvider.getExpirationTimeFromToken(newAccessToken);
        java.time.Instant newRefreshExpiry = jwtTokenProvider.getExpirationTimeFromToken(newRefreshToken);

        // 기존 토큰 밀어내기 Rotation
        JwtInformation newInfo = new JwtInformation(userDetails.getUserDto(), newAccessToken, newRefreshToken, newAccessExpiry, newRefreshExpiry);
        jwtRegistry.rotateJwtInformation(refreshToken, newInfo);

        ResponseCookie refreshTokenCookie = ResponseCookie.from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, newRefreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 전송
                .sameSite("Strict") // CSRF 공격 방지
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(new JwtDto(userDetails.getUserDto(), newAccessToken));
    }

    @PutMapping(path = "role")
    public ResponseEntity<UserDto> updateRole(@RequestBody @Valid UserRoleUpdateRequest request) {
        UserDto updated = userService.updateRole(request.userId(), request.newRole());
        return ResponseEntity.ok(updated);
    }
}
