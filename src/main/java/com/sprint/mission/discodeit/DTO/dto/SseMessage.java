package com.sprint.mission.discodeit.dto.dto;

import java.util.UUID;

public record SseMessage(
        UUID id,
        String eventName,
        Object data
) {
}
