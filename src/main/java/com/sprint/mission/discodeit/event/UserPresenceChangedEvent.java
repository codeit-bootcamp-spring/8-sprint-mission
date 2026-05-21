package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.user.UserDto;

public record UserPresenceChangedEvent(UserDto userDto) {}
