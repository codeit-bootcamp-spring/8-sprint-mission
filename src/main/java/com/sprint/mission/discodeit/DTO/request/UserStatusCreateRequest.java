package com.sprint.mission.discodeit.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "생성할 UserStatus 정보")
public record UserStatusCreateRequest(
    UUID userId,
    Instant lastActiveAt
) {

}
