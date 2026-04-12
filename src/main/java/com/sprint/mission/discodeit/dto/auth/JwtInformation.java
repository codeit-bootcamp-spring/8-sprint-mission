package com.sprint.mission.discodeit.dto.auth;

import com.sprint.mission.discodeit.dto.user.UserDto;
import java.time.Instant;
import lombok.Getter;

/**
 * 하나의 로그인 세션에서 발행된 토큰 쌍과
 * 사용자 정보를 묶어서 관리하는 곳.
 */
@Getter
public class JwtInformation {

  private final UserDto userDto; // 토큰의 주인 정보
  private String accessToken;    // 현재 유효한 액세스 토큰
  private String refreshToken;   // 현재 유효한 리프레시 토큰

  /**
   * 요구사항에는 없으나 'clearExpiredJwtInformation' 기능을
   * 구현하기 위해 만료 시간을 비교할 기준값 추가
   */
  private Instant expiresAt;

  public JwtInformation(UserDto userDto, String accessToken, String refreshToken, Instant expiresAt) {
    this.userDto = userDto;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiresAt = expiresAt;
  }

  /**
   * 리프레시 토큰 로테이션 시 호출
   * 기존 객체를 유지하면서 내부의 토큰 값들만 새것으로 교체
   */
  public void rotate(String accessToken, String refreshToken, Instant expiresAt) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiresAt = expiresAt;
  }

}
