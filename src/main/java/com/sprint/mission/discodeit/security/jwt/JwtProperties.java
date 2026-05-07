package com.sprint.mission.discodeit.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,
    long accessTokenExpiry,
    long refreshTokenExpiry
) {

}
