package com.sprint.mission.discodeit.event.Sse.User;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.event.CreatedEvent;

import java.time.Instant;

public class UserCreatedEvent extends CreatedEvent<UserDto> {

    public UserCreatedEvent(UserDto data, Instant createdAt) {
        super(data, createdAt);
    }
}
