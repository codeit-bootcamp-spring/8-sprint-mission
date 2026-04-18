package com.sprint.mission.discodeit.dto.dto;

public record JwtDTO(
        UserDto userDto,
        String accessToken
) {
}
