package com.sprint.mission.discodeit.event.message;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import java.time.Instant;
import java.util.List;

public class NotificationCreatedEvent extends CreatedEvent<List<NotificationDto>> {

  public NotificationCreatedEvent(List<NotificationDto> data, Instant createdAt) {
    super(data, createdAt);
  }
}
