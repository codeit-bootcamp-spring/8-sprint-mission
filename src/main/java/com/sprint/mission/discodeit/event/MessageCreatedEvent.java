package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprint.mission.discodeit.dto.dto.MessageDto;

import java.time.Instant;

// 
public class MessageCreatedEvent extends CreatedEvent<MessageDto> {
    @JsonCreator
    public MessageCreatedEvent(
            @JsonProperty("data") MessageDto data,
            @JsonProperty("createdAt") Instant createdAt
    ) {
        super(data, createdAt);
    }
}
