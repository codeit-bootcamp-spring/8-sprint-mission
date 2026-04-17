package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.dto.JwtDTO;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.store.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.service.UserService;
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

    private final UserService userService;
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

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     * <p>
     * 리프레시 토큰을 사용해 액세스 토큰을 재발급하는 API.
     * 리프레시 토큰은 쿠키에 저장되어 있으며, 이를 통해 액세스 토큰을 재발급한다.
     *
     * @param refreshToken 리프레시 토큰
     * @param response     Http 응답 객체
     * @return 재발급된 액세스 토큰
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtDTO> refresh(
            // @CookieValue 어노테이션을 사용하면 HTTP 요청 헤더(Cookie)의 쿠키 값을 자동으로 추출해준다.
            @CookieValue(
                    // 쿠키 이름은 JwtTokenProvider 클래스에 정의된 상수 REFRESH_TOKEN_COOKIE_NAME을 사용한다.
                    name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                    // 쿠키 값이 없어도 에러를 발생시키지 않고 null을 반환한다. 그러면 아래 코드에서 쿠기 값이 null인지 확인하고 400 응답을 반환한다.
                    required = false
            )
            String refreshToken,
            HttpServletResponse response) {

        // 쿠키 값이 없거나(null) 유효하지 않으면 401 응답을 반환한다.
        if (refreshToken == null || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
            log.warn("[AuthController] JwtRegistry에 없는 리프레시 토큰 재사용 시도 차단");
            jwtTokenProvider.expireRefreshCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 쿠키 값이 유효하면 쿠키 값을 추출한다.
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // 사용자 로드
        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) userDetailsService.loadUserByUsername(username);

        try {
            // 새 토큰 발급
            String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            JwtInformation newInformation = new JwtInformation(
                    userDetails.getUserDto(),
                    newAccessToken,
                    newRefreshToken
            );
            jwtRegistry.rotateJwtInformation(refreshToken, newInformation);
            log.info("[AuthController] JwtRegistry 토큰 교체(로테이션) 완료");

            // 리프레시 쿠키 교체
            // HTTP 응답 헤더(Set-Cookie)에 리프레시 쿠키를 추가한다.
            jwtTokenProvider.addRefreshCookie(response, newRefreshToken);

            // 응답 바디 구성
            UserDto userDto = userDetails.getUserDto();
            JwtDTO body = new JwtDTO(userDto, newAccessToken);

            // 응답 바디 전송
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/role")
    public ResponseEntity<UserDto> updateUserRole(@RequestBody UserRoleUpdateRequest userRoleUpdateRequest) {
        log.info("[AuthController] 사용자 권한 변경 요청 접수됨...");

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
