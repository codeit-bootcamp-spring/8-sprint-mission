package com.sprint.mission.discodeit.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discodeit.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @Positive long accessExpirationMs,
        @Positive long refreshExpirationMs,
        @Positive int maxActiveJwtCount
) {
}
