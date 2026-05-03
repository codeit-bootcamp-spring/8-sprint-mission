package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.auth.JwtInformation;
import java.util.Optional;
import java.util.UUID;

public interface JwtRegistry {
  void registerJwtInformation(UUID userId, JwtInformation info);
  void invalidateJwtInformationByUserId(UUID userId);
  void invalidateJwtInformationByRefreshToken(String refreshToken);
  boolean hasActiveJwtInformationByUserId(UUID userId);
  boolean hasActiveJwtInformationByAccessToken(String accessToken);
  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);
  void rotateJwtInformation(UUID userId, String oldRefreshToken, JwtInformation newInfo);
  void clearExpiredJwtInformation();
  Optional<JwtInformation> findJwtInformationByRefreshToken(String refreshToken);
}
