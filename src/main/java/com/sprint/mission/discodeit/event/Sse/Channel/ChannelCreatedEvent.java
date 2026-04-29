package com.sprint.mission.discodeit.event.Sse.Channel;

import com.sprint.mission.discodeit.dto.dto.ChannelDto;

import java.util.Set;
import java.util.UUID;

public record ChannelCreatedEvent(
        Set<UUID> receiverIds,
        ChannelDto channelDto
) {
}
