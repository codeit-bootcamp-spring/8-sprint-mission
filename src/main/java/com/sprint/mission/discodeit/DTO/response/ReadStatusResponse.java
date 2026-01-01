package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusResponse(
        UUID id,
        UUID userId,
        UUID channelId,
        Instant createdAt,
        Instant lastReadMessageAt
) {
    public static ReadStatusResponse from(ReadStatus readStatus) {
        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getCreatedAt(),
                readStatus.getLastReadMessageAt()
        );
    }
}
