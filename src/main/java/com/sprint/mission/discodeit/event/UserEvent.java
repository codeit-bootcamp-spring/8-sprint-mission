package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record UserEvent(
    UUID userId,
    UserEventType type
) {
  public enum UserEventType {
    CREATED, UPDATED, DELETED
  }
}
