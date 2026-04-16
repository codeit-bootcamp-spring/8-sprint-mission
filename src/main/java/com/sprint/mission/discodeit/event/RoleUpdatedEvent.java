package com.sprint.mission.discodeit.event;

import java.time.Instant;
import java.util.UUID;

public record RoleUpdatedEvent(
    UUID userId,
    String userName,
    String previousRole,
    String newRole,
    Instant occurredAt
) {

  public static RoleUpdatedEvent now(
      UUID userId, String userName, String previousRole, String newRole
  ) {
    return new RoleUpdatedEvent(
        userId, userName, previousRole, newRole, Instant.now()
    );
  }
}
