package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.UUID;

public record RoleUpdatedEvent(
    UUID userId,
    User user,
    String previousRole,
    String newRole,
    Instant occurredAt
) {

  public static RoleUpdatedEvent now(
      UUID userId, User user, String previousRole, String newRole
  ) {
    return new RoleUpdatedEvent(
        userId, user, previousRole, newRole, Instant.now()
    );
  }
}
