package com.sprint.mission.discodeit.DTO.dto;

public record JwtDTO(
        UserDto userDto,
        String accessToken
) {
}
