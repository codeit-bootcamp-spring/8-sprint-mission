package com.sprint.mission.discodeit.DTO.request;

import java.util.Optional;
import java.util.UUID;

public record UserUpdateRequest(
        UUID id,
        String userId,
        String name,
        String password,
        String email,
        char gender,
        int grade,
        Optional<UUID> profileId
) {
}
