package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtInformation {

  private volatile UserResponse userResponse;
  private volatile String accessToken;
  private volatile String refreshToken;
  private volatile Instant accessTokenExpiry;
  private volatile Instant refreshTokenExpiry;

  // 토큰 로테이션 갱신
  public void rotateToken(UserResponse userResponse, String newAccessToken, String newRefreshToken,
      Instant newAccessTokenExpiry, Instant newRefreshTokenExpiry) {

    this.userResponse = userResponse;
    this.accessToken = newAccessToken;
    this.refreshToken = newRefreshToken;
    this.accessTokenExpiry = newAccessTokenExpiry;
    this.refreshTokenExpiry = newRefreshTokenExpiry;
  }

  public boolean isActive() {
    Instant now = Instant.now();
    return (accessTokenExpiry != null && accessTokenExpiry.isAfter(now)) ||
        (refreshTokenExpiry != null && refreshTokenExpiry.isAfter(now));
  }
}
