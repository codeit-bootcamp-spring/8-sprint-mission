package com.sprint.mission.discodeit.domain.channel.event;

import com.sprint.mission.discodeit.domain.channel.dto.domain.ChannelDto;

import java.time.Instant;

public record ChannelEvent
(
        String eventName,
        ChannelDto channelDto,
        Instant createdAt
) {}
