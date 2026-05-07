package com.sprint.mission.discodeit.security.jwt;

import java.util.UUID;

public interface JwtRegistry {

  // 로그인 성공 시 토큰 등록
  void registerJwtInformation(JwtInformation jwtInformation);

  // 권한 변경 시 해당 유저 토큰 전체 무효화
  void invalidateJwtInformationByUserId(UUID userId);

  // 현재 유저가 로그인 중인지 확인
  boolean hasActiveJwtInformationByUserId(UUID userId);

  // API 요청 시 유효한 액세스 토큰인지 확인 (블랙리스트)
  boolean hasActiveJwtInformationByAccessToken(String accessToken);

  // 토큰 재발급 시 유효한 리프레시 토큰인지 확인
  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

  // 토큰 재발급 후 레지스트리 정보를 새 토큰으로 교체 (Rotation)
  void rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation);

  // 특정 리프레시 토큰을 무효화 (기기 하나 로그아웃용)
  void invalidateJwtInformationByRefreshToken(String refreshToken);

  // 만료된 토큰 정보 정리 (스케줄러)
  void clearExpiredJwtInformation();
}
