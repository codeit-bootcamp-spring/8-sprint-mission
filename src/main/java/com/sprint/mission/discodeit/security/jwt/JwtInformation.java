package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.data.UserDto;
import java.time.Instant;

public class JwtInformation {
    private final UserDto userDto;
    private final String accessToken;
    private final String refreshToken;
    private final Instant accessTokenExpiry;
    private final Instant refreshTokenExpiry;

    public JwtInformation(UserDto userDto, String accessToken, String refreshToken, Instant accessTokenExpiry, Instant refreshTokenExpiry) {
        this.userDto = userDto;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public UserDto getUserDto() {
        return userDto;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public Instant getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }

    public boolean isAccessTokenExpired() {
        return accessTokenExpiry != null && accessTokenExpiry.isBefore(Instant.now());
    }

    public boolean isRefreshTokenExpired() {
        return refreshTokenExpiry != null && refreshTokenExpiry.isBefore(Instant.now());
    }

    public boolean isActive() {
        return !isRefreshTokenExpired();
    }
}
