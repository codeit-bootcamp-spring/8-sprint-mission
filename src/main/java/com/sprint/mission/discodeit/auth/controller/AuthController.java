package com.sprint.mission.discodeit.auth.controller;

import com.sprint.mission.discodeit.auth.constants.AuthConstants;
import com.sprint.mission.discodeit.auth.dto.AuthSuccessResponse;
import com.sprint.mission.discodeit.auth.dto.CsrfTokenResponse;
import com.sprint.mission.discodeit.auth.dto.JwtDTO;
import com.sprint.mission.discodeit.auth.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.user.InvalidCredentialsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.security.store.JwtSessionRegistry;
import com.sprint.mission.discodeit.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
  private final UserService userService;
  private final DiscodeitUserDetailsService userDetailsService;
  private final SessionRegistry sessionRegistry;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtSessionRegistry jwtSessionRegistry;

  @GetMapping("/csrf-token")
  public ResponseEntity<AuthSuccessResponse<CsrfTokenResponse>> getCsrfToken(CsrfToken csrfToken) {
    CsrfTokenResponse tokenResponse = new CsrfTokenResponse(csrfToken.getToken(),
        csrfToken.getHeaderName(), csrfToken.getParameterName());

    AuthSuccessResponse<CsrfTokenResponse> response = new AuthSuccessResponse<>(tokenResponse);

    log.info("{} CSRF 토큰 발급 완료", AuthConstants.LOG_PREFIX_CSRF_TOKEN);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/role")
  public ResponseEntity<UserDto> updateRole(@RequestBody UserRoleUpdateRequest request,
      @AuthenticationPrincipal DiscodeitUserDetails currentUser) {

    UserDto updatedUser = userService.updateRole(request.userId(), request.newRole());

    // 변경 대상이 현재 요청자 본인인 경우: SecurityContext 즉시 갱신 (재로그인 불필요)
    if (currentUser.getUser().getId().equals(request.userId())) {
      UserDetails refreshed = userDetailsService.loadUserByUsername(updatedUser.username());
      Authentication newAuth = new UsernamePasswordAuthenticationToken(refreshed, null,
          refreshed.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(newAuth);
      log.info("{} 현재 세션 Authentication 갱신 완료: username={}", AuthConstants.LOG_PREFIX_CSRF_TOKEN,
          updatedUser.username());
    } else {
      // 다른 사용자의 Role 변경: 해당 사용자의 세션 만료 → 재로그인 유도
      expireSessionsByUsername(updatedUser.username());
    }

    log.info("{} 사용자 Role 변경 완료: {}", AuthConstants.LOG_PREFIX_CSRF_TOKEN, updatedUser.email());
    return ResponseEntity.ok(updatedUser);
  }

  /**
   * SessionRegistry에 등록된 특정 사용자의 모든 활성 세션을 만료시킨다.
   * 세션 만료 후 해당 사용자는 다음 요청 시 재로그인이 필요하다.
   */
  private void expireSessionsByUsername(String username) {
    sessionRegistry.getAllPrincipals().stream().filter(
            principal -> principal instanceof UserDetails ud && ud.getUsername().equals(username))
        .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
        .forEach(sessionInfo -> {
          sessionInfo.expireNow();
          log.info("{} 세션 만료 처리: username={}, sessionId={}", AuthConstants.LOG_PREFIX_CSRF_TOKEN,
              username, sessionInfo.getSessionId());
        });
  }

  // 리프레시 토큰을 활용한 엑세스 토큰 재발급
  @PostMapping("/refresh")
  public ResponseEntity<JwtDTO> refreshAccessToken(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) final String refreshToken,
      HttpServletResponse response
  ) {

    if (!StringUtils.hasText(refreshToken) || !jwtTokenProvider.validateRefreshToken(
        refreshToken)) {
      throw InvalidCredentialsException.wrongPassword();
    }

    final String oldRefreshTokenJti = jwtTokenProvider.getTokenId(refreshToken);
    if (jwtSessionRegistry.isRevoked(oldRefreshTokenJti)) {
      throw InvalidCredentialsException.wrongPassword();
    }

    final String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
    final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    if (!(userDetails instanceof DiscodeitUserDetails discodeitUserDetails)) {
      throw InvalidCredentialsException.wrongPassword();
    }

    final String accessToken;
    final String rotatedRefreshToken;

    try {
      accessToken = jwtTokenProvider.generateAccessToken(discodeitUserDetails);
      rotatedRefreshToken = jwtTokenProvider.generateRefreshToken(discodeitUserDetails);
    } catch (JOSEException exception) {
      throw new DiscodeitException(ErrorCode.INTERNAL_SERVER_ERROR, exception);
    }

    jwtTokenProvider.addRefreshCookie(response, rotatedRefreshToken);

    jwtSessionRegistry.register(jwtTokenProvider.toEntity(accessToken));
    final String newRefreshTokenJti = jwtTokenProvider.getTokenId(rotatedRefreshToken);
    jwtSessionRegistry.register(jwtTokenProvider.toEntity(rotatedRefreshToken));
    jwtSessionRegistry.markReplaced(oldRefreshTokenJti, newRefreshTokenJti);

    final UserDto userDto = userMapper.toDto(discodeitUserDetails.getUser());
    final JwtDTO jwtDto = new JwtDTO(userDto, accessToken);

    log.info("{} access token 재발급 완료: username={}", AuthConstants.LOG_PREFIX_CSRF_TOKEN, username);

    return ResponseEntity.ok(jwtDto);
  }
}
