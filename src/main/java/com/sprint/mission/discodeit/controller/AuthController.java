package com.sprint.mission.discodeit.controller;

import com.nimbusds.jose.JOSEException;
import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.JwtDTO;
import com.sprint.mission.discodeit.dto.JwtInformation;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController implements AuthApi {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final JwtRegistry jwtRegistry;

  @GetMapping("csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);

    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        .build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDTO> refresh(
      @CookieValue(
          name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
          required = false
      )
      String refreshToken,
      HttpServletResponse response) {

    if (refreshToken == null || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
      log.warn("[AuthController] 유효하지 않거나 누락된 리프레시 토큰입니다. Token: {}",
          (refreshToken == null ? "null" : "invalid_format"));
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      log.warn("[AuthController] 레지스트리에 존재하지 않는 리프레시 토큰입니다. (이미 만료되었거나 비정상 접근)");

      jwtTokenProvider.expireRefreshCookie(response);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
    DiscodeitUserDetails discodeitUserDetails = (DiscodeitUserDetails) userDetailsService.loadUserByUsername(
        username);

    try {
      String newAccessToken = jwtTokenProvider.generateAccessToken(discodeitUserDetails);
      String newRefreshToken = jwtTokenProvider.generateRefreshToken(discodeitUserDetails);

      JwtInformation newJwtInformation = new JwtInformation(
          discodeitUserDetails.getUserDto(),
          newAccessToken,
          newRefreshToken
      );

      jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

      // 쿠키를 응답에 추가
      jwtTokenProvider.addRefreshCookie(response, newRefreshToken);

      UserDto userDto = discodeitUserDetails.getUserDto();
      JwtDTO jwtDTO = new JwtDTO(userDto, newAccessToken);

      return ResponseEntity
          .status(HttpStatus.OK)
          .body(jwtDTO);

    } catch (JOSEException e) {
      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  @PutMapping("/role")
  public ResponseEntity<UserDto> updateUserRole(
      @Valid @RequestBody UserRoleUpdateRequest userRoleUpdateRequest) {

    UserDto userDto = authService.updateUserRole(userRoleUpdateRequest);
    log.info("[AuthController] 권한 변경 성공: userId={}, newRole={}",
        userRoleUpdateRequest.userId(), userRoleUpdateRequest.newRole());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDto);
  }
}

