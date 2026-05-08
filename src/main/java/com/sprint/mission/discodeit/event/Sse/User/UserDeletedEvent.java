package com.sprint.mission.discodeit.event.Sse.User;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.event.DeletedEvent;

import java.time.Instant;

public class UserDeletedEvent extends DeletedEvent<UserDto> {

    public UserDeletedEvent(UserDto userDto, Instant deletedAt) {
        super(userDto, deletedAt);
    }
}
