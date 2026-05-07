package com.sprint.mission.discodeit.global.sse.dto;

import java.util.UUID;

public record SseMessage
(
        UUID id,
        String eventName,
        Object data
) {}
