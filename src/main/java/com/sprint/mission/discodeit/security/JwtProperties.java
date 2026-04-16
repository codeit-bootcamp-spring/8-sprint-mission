package com.sprint.mission.discodeit.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jwt")
@Validated
public record JwtProperties(
    @Valid TokenProperties accessToken,
    @Valid TokenProperties refreshToken
) {

  public record TokenProperties(

      @NotBlank(message = "시크릿 키 값은 필수입니다.")
      String secret,

      @Positive(message = "토큰 기간은 양수여야 합니다.")
      int exp
  ) {

  }
}
