package com.sprint.mission.discodeit.DTO.response;

public record LoginResponse(
        String message,
        UserResponse user
) {
}
