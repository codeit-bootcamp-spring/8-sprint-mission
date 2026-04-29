package com.sprint.mission.discodeit.event.Sse;

import com.sprint.mission.discodeit.dto.dto.NotificationDto;

public record NotificationCreatedEvent(
        NotificationDto notificationDto
) {
}
