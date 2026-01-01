package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserStatusResponse(
        UUID id,
        UUID userId,
        Instant createdAt,
        Instant LastAccessAt
) {
    public static UserStatusResponse from(UserStatus status) {
        return new UserStatusResponse(
                status.getId(),
                status.getUserId(),
                status.getCreatedAt(),
                status.getLastAccessAt()
        );
    }
}
