package com.sprint.mission.discodeit.DTO.request;

import java.util.UUID;

public record MessageCreateRequest(
        UUID userId,
        UUID channelId,
        String content
) {
}
