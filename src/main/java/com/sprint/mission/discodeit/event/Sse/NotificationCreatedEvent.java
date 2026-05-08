package com.sprint.mission.discodeit.event.Sse;

import com.sprint.mission.discodeit.dto.dto.NotificationDto;
import com.sprint.mission.discodeit.event.CreatedEvent;

import java.time.Instant;
import java.util.List;

public class NotificationCreatedEvent extends CreatedEvent<List<NotificationDto>> {
    public NotificationCreatedEvent(List<NotificationDto> data, Instant createdAt) {
        super(data, createdAt);
    }
}
