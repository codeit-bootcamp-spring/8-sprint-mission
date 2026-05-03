package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprint.mission.discodeit.dto.data.UserDto;
import java.time.Instant;

public class JwtInformation {
    private final UserDto userDto;
    private String accessToken;
    private String refreshToken;
    private Instant accessTokenExpiry;
    private Instant refreshTokenExpiry;

    @JsonCreator
    public JwtInformation(
            @JsonProperty("userDto") UserDto userDto,
            @JsonProperty("accessToken") String accessToken,
            @JsonProperty("refreshToken") String refreshToken,
            @JsonProperty("accessTokenExpiry") Instant accessTokenExpiry,
            @JsonProperty("refreshTokenExpiry") Instant refreshTokenExpiry) {
        this.userDto = userDto;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    /** Redis 등에서 동일 엔트리를 갱신할 때 사용 (리스트 내 원소 갱신). */
    public void rotate(
            String newAccessToken,
            String newRefreshToken,
            Instant newAccessTokenExpiry,
            Instant newRefreshTokenExpiry) {
        this.accessToken = newAccessToken;
        this.refreshToken = newRefreshToken;
        this.accessTokenExpiry = newAccessTokenExpiry;
        this.refreshTokenExpiry = newRefreshTokenExpiry;
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
