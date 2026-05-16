package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.data.UserDto;
import java.time.Instant;

public class UserDeletedEvent extends DeletedEvent<UserDto> implements UserEvent {


  public UserDeletedEvent(UserDto data, Instant deletedAt) {
    super(data, deletedAt);
  }
}
