package com.sprint.mission.discodeit.auth.dto;

import com.sprint.mission.discodeit.dto.data.UserDto;

/**
 * JWT 응답 DTO
 * 로그인/재발급 시 사용자 정보와 액세스 토큰을 함께 반환한다.
 */
public class JwtDTO {

  private UserDto user;
  private String accessToken;

  public JwtDTO() {
  }

  public JwtDTO(UserDto user, String accessToken) {
    this.user = user;
    this.accessToken = accessToken;
  }

  public UserDto getUser() {
    return user;
  }

  public void setUser(UserDto user) {
    this.user = user;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public String toString() {
    return "JwtDTO{" + "user=" + user + ", accessToken='" + accessToken + '\'' + '}';
  }
}


