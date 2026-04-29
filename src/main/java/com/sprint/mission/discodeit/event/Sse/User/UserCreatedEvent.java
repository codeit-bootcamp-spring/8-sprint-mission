package com.sprint.mission.discodeit.event.Sse.User;

import com.sprint.mission.discodeit.dto.dto.UserDto;

public record UserCreatedEvent(
        UserDto userDto
) {
}
