package com.sprint.mission.discodeit.event.Sse.Channel;

import com.sprint.mission.discodeit.dto.dto.ChannelDto;

public record ChannelUpdatedEvent(
        ChannelDto channelDto
) {
}
