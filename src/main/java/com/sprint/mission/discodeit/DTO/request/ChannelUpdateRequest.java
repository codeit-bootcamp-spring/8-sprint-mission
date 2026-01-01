package com.sprint.mission.discodeit.DTO.request;

import java.util.List;
import java.util.UUID;

public record ChannelUpdateRequest(
        String channelName,
        String description
) {
}
