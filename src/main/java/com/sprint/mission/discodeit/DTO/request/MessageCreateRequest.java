package com.sprint.mission.discodeit.DTO.request;

import java.util.UUID;

public record MessageCreateRequest(
        UUID uId,
        UUID cId,
        String channelName,
        String from,
        String content
) {
}
