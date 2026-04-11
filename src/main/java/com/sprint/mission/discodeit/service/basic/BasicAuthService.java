package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.auth.AuthException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailsService userDetailsService;

  @Override
  @Transactional
  public JwtDto refresh(String refreshToken, HttpServletResponse response) {

    if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
      throw new AuthException(ErrorCode.INVALID_TOKEN, null);
    }

    UUID userId = UUID.fromString(jwtTokenProvider.extractSubject(refreshToken));
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) userDetailsService.loadUserById(userId);
    UserDto userDto = userDetails.getUserDto();

    // 토큰 재발급
    String newAccessToken = jwtTokenProvider.createAccessToken(userId, userDto.role().name());
    String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, userDto.role().name());

    // 쿠키 설정
    Cookie refreshCookie = new Cookie("REFRESH_TOKEN", newRefreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(604800);
    response.addCookie(refreshCookie);

    return new JwtDto(newAccessToken, userDto);
  }
}
