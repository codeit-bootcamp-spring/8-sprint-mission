package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.data.UserDto;
import java.time.Instant;

public class UserUpdatedEvent extends UpdatedEvent<UserDto> implements UserEvent {

  public UserUpdatedEvent(UserDto from, UserDto to,
      Instant updatedAt) {
    super(from, to, updatedAt);
  }
}
