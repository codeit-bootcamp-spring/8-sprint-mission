package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.data.JwtInformation;
import java.util.UUID;

public interface JwtRegistry {

  //등록 및 동시 로그인 제어
  void registerJwtInformation(JwtInformation jwtInformation);

  // 무효화 (로그아웃이나 권한 변경 시)
  void invalidateJwtInformationByUserId(UUID userId);

  // 상태 확인
  boolean hasActiveJwtInformationByUserId(UUID userId);

  boolean hasActiveJwtInformationByAccessToken(String accessToken);

  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

  //로테이션 (재발급 시)
  void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation);

  // 청소 (스케줄러에서 활용)
  void clearExpiredJwtInformation();

  //로그아웃 시 리프레시 토큰으로 무효화하기 위해 추가
  void invalidateJwtInformationByRefreshToken(String refreshToken);
}
