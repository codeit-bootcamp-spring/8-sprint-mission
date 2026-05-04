package com.sprint.mission.discodeit.dto.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        Instant createdAt,
        // 알람을 수신할 User의 Id
        UUID receiverId,
        String title,
        String content
) {
}
