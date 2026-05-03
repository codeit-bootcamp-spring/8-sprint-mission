package com.sprint.mission.discodeit.event;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentCreatedEvent(
    UUID binaryContentId,
    String fileName,
    byte[] data,
    Instant occurredAt
) {

  public static BinaryContentCreatedEvent now(
      UUID binaryContentId, String fileName, byte[] data
  ) {

    return new BinaryContentCreatedEvent(
        binaryContentId, fileName, data, Instant.now()
    );
  }
}
