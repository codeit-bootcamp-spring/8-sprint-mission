package com.sprint.mission.discodeit.domain.message.event;

import com.sprint.mission.discodeit.domain.message.dto.domain.MessageDto;

import java.util.UUID;

public record MessageCreatedEvent
(
        UUID authorId,
        UUID channelId,
        String authorName,
        String channelName,
        MessageDto data
) {}
