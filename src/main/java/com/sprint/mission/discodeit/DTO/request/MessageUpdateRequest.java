package com.sprint.mission.discodeit.DTO.request;

import java.util.UUID;

public record MessageUpdateRequest(
        UUID id,
        String content
) {
}
