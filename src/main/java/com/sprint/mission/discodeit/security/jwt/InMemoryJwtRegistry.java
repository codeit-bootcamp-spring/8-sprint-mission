package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.data.JwtInformation;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount = 1;

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {

  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {

  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    return false;
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return false;
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return false;
  }

  @Override
  public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {

  }

  @Override
  public void clearExpiredJwtInformation() {

  }
}
