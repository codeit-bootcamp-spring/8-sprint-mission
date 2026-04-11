package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.JwtInformation;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InMemoryJwtRegistry implements JwtRegistry {

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {

  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userID) {

  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userID) {
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
