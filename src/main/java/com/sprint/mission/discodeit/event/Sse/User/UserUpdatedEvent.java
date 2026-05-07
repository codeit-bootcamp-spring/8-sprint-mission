package com.sprint.mission.discodeit.event.Sse.User;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.event.UpdatedEvent;

import java.time.Instant;

public class UserUpdatedEvent extends UpdatedEvent<UserDto> {

    public UserUpdatedEvent(UserDto from, UserDto to, Instant updatedAt) {
        super(from, to, updatedAt);
    }
}
