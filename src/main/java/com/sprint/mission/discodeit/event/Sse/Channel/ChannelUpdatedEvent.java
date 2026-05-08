package com.sprint.mission.discodeit.event.Sse.Channel;

import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.event.UpdatedEvent;

import java.time.Instant;

public class ChannelUpdatedEvent extends UpdatedEvent<ChannelDto> {

    public ChannelUpdatedEvent(ChannelDto from, ChannelDto to, Instant updatedAt) {
        super(from, to, updatedAt);
    }
}
