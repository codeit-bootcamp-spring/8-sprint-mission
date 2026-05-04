package com.sprint.mission.discodeit.event.Sse.User;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.event.CreatedEvent;

import java.time.Instant;

public class UserUpdatedEvent extends CreatedEvent<UserDto> {

    public UserUpdatedEvent(UserDto userDto, Instant deletedAt) {
        super(userDto, deletedAt);
    }
}
