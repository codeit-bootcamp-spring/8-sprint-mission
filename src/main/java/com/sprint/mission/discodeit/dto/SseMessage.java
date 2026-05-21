package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;

public record SseMessage(
    UUID id,
    String eventName,
    Object data,
    Instant createdAt
) {

}
