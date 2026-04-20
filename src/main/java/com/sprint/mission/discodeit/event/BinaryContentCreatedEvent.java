package com.sprint.mission.discodeit.event;

import java.util.UUID;
import lombok.Getter;

@Getter
public class BinaryContentCreatedEvent {

  private final UUID binaryContentId;
  private final byte[] bytes;

  public BinaryContentCreatedEvent(UUID binaryContentId, byte[] bytes) {
    this.binaryContentId = binaryContentId;
    this.bytes = bytes;
  }
}
