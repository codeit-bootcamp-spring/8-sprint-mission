package com.sprint.mission.discodeit.event;

import java.time.Instant;
import java.util.UUID;

public record MessageCreatedEvent(
    UUID messageId,
    String content,
    UUID channelId,
    String channelName,
    UUID authorId,
    String authorName,
    Instant occurredAt
) {

  public static MessageCreatedEvent now(
      UUID messageId, String content, UUID channelId, String channelName, UUID authorId,
      String authorName
  ) {
    return new MessageCreatedEvent(
        messageId, content, channelId, channelName, authorId, authorName, Instant.now()
    );
  }
}
