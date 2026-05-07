package com.sprint.mission.discodeit.domain.user.event;

import com.sprint.mission.discodeit.domain.user.dto.domain.UserDto;

import java.time.Instant;

public record UserEvent
(
        String eventName,
        UserDto userDto,
        Instant createdAt
) {}
