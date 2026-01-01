package com.sprint.mission.discodeit.DTO.request;

import com.sprint.mission.discodeit.entity.ChannelStatus;

import java.util.List;
import java.util.UUID;

public record ChannelCreateRequest(
        UUID userId,
        ChannelStatus status,
        String name,
        String description,
        List<UUID> participantsIds
) {
}
