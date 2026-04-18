package com.sprint.mission.discodeit.dto;

public record JwtInformation(
    UserDto userDto,
    String accessToken,
    String refreshToken
) {
  
}
