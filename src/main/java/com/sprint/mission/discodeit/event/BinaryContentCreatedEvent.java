package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContent;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BinaryContentCreatedEvent {

  private final BinaryContent binaryContent;
  private final Instant createdAt;
  private final byte[] bytes;

  public BinaryContentCreatedEvent(BinaryContent binaryContent, Instant createdAt, byte[] bytes) {
    this.binaryContent = binaryContent;
    this.createdAt = createdAt;
    this.bytes = bytes;
  }
}
