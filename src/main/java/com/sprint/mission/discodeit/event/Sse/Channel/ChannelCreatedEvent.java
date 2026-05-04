package com.sprint.mission.discodeit.event.Sse.Channel;

import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.event.CreatedEvent;

import java.time.Instant;

public class ChannelCreatedEvent extends CreatedEvent<ChannelDto> {

    public ChannelCreatedEvent(ChannelDto data, Instant createdAt) {
        super(data, createdAt);
    }
}
