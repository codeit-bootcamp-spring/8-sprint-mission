package com.sprint.mission.discodeit.dto.kafka;

import java.time.Instant;
import java.util.UUID;

public record MessageCreatedKafkaEvent(
    UUID messageId,
    UUID channelId,
    UUID authorId,
    String content,
    String authorName,
    String channelName,
    Instant createdAt
) {
}
