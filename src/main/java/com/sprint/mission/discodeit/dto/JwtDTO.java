package com.sprint.mission.discodeit.dto;

public record JwtDTO(
    UserDto userDto,
    String accessToken
) {

}
