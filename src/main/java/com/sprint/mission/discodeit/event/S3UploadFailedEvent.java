package com.sprint.mission.discodeit.event;

import java.time.Instant;
import java.util.UUID;

public record S3UploadFailedEvent(
    UUID binaryContentId,
    String requestId,
    String errorMessage,
    Instant occurredAt
) {

  public static S3UploadFailedEvent now(
      UUID binaryContentId, String requestId, String errorMessage
  ) {
    return new S3UploadFailedEvent(
        binaryContentId, requestId, errorMessage, Instant.now()
    );
  }
}
