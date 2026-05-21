package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record NotificationCreatedEvent(
    UUID notificationId,
    UUID receiverId
) {

}
