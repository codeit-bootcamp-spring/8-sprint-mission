package com.sprint.mission.discodeit.event;

import java.time.Instant;
import java.util.UUID;

public record MessageCreatedEvent(
    UUID messageId,
    String content,
    String channelName,
    String authorName,
    Instant occurredAt
) {

  public static MessageCreatedEvent now(
      UUID messageId, String content, String channelName, String authorName
  ) {
    return new MessageCreatedEvent(
        messageId, content, channelName, authorName, Instant.now()
    );
  }
}
