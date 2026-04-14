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
      UUID messageId, String content, String channelName, String authorName
  ) {
    return new RoleUpdatedEvent(
        messageId, content, channelName, authorName, Instant.now()
    );
  }
}
