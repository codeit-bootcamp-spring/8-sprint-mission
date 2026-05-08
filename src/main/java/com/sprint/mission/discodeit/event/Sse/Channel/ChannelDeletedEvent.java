package com.sprint.mission.discodeit.event.Sse.Channel;

import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.event.DeletedEvent;

import java.time.Instant;

public class ChannelDeletedEvent extends DeletedEvent<ChannelDto> {

    public ChannelDeletedEvent(ChannelDto channelDto, Instant deletedAt) {
        super(channelDto, deletedAt);
    }
}
