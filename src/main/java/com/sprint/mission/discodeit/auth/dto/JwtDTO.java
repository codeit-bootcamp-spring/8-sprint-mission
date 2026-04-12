package com.sprint.mission.discodeit.auth.dto;

import com.sprint.mission.discodeit.dto.data.UserDto;

/**
 * JWT 응답 DTO
 * 로그인/재발급 시 사용자 정보와 액세스 토큰을 함께 반환한다.
 * 프론트엔드는 { userDto, accessToken } 구조를 기대한다.
 */
public class JwtDTO {

  private UserDto userDto;
  private String accessToken;

  public JwtDTO() {
  }

  public JwtDTO(UserDto userDto, String accessToken) {
    this.userDto = userDto;
    this.accessToken = accessToken;
  }

  public UserDto getUserDto() {
    return userDto;
  }

  public void setUserDto(UserDto userDto) {
    this.userDto = userDto;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public String toString() {
    return "JwtDTO{" + "userDto=" + userDto + ", accessToken='" + accessToken + '\'' + '}';
  }
}
