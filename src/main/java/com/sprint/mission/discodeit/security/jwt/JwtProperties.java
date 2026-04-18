package com.sprint.mission.discodeit.security.jwt;

import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    AccessToken accessToken,
    RefreshToken refreshToken
) {

  public record AccessToken(
      @Size(min = 32, message = "Access Token Secret은 최소 32자 이상이어야 합니다.")
      String secret,
      int exp
  ) {

  }

  public record RefreshToken(
      @Size(min = 32, message = "Refresh Token Secret은 최소 32자 이상이어야 합니다.")
      String secret,
      int exp
  ) {

  }
}
