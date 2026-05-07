package com.sprint.mission.discodeit.domain.notification.event;

import com.sprint.mission.discodeit.domain.notification.dto.entity.NotificationDto;

import java.time.Instant;

public record NotificationCreatedEvent
(
        String eventName,
        NotificationDto notification,
        Instant createdAt
) {}
