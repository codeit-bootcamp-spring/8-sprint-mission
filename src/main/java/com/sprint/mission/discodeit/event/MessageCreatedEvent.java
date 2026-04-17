package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.dto.MessageDto;

import java.time.Instant;

public class MessageCreatedEvent extends CreatedEvent<MessageDto> {

    public MessageCreatedEvent(MessageDto data, Instant createdAt) {
        super(data, createdAt);
    }
}
