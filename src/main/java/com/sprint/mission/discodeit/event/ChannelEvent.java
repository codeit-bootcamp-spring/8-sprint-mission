package com.sprint.mission.discodeit.event;

import java.util.List;
import java.util.UUID;

public record ChannelEvent(
    UUID channelId,
    ChannelEventType type,
    List<UUID> memberIds
) {
  public enum ChannelEventType {
    CREATED, UPDATED, DELETED
  }
}
