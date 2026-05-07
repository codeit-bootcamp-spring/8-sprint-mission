package com.sprint.mission.discodeit.domain.user.event;

import com.sprint.mission.discodeit.domain.user.dto.domain.UserDto;

import java.time.Instant;

public record UserDeletedEvent
(
        UserDto data,
        Instant deletedAt
) {}
