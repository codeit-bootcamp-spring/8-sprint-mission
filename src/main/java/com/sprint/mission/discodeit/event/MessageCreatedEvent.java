package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.UUID;

public record MessageCreatedEvent(
    UUID messageId,
    String content,
    Channel channel,
    User author,
    Instant occurredAt
) {

  public static MessageCreatedEvent now(
      UUID messageId, String content, Channel channel, User author
  ) {
    return new MessageCreatedEvent(
        messageId, content, channel, author, Instant.now()
    );
  }
}
