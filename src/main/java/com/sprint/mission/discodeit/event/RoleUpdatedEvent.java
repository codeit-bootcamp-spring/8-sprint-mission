package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import lombok.Getter;

@Getter
public class RoleUpdatedEvent {

  private final User user;
  private final Role oldRole;
  private final Role newRole;

  public RoleUpdatedEvent(User user, Role oldRole, Role newRole) {
    this.user = user;
    this.oldRole = oldRole;
    this.newRole = newRole;
  }
}
