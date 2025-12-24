package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusResponse(
        UUID id,
        UUID uId,
        UUID cId,
        Instant createdAt,
        Instant modifiedAt,
        Instant lastReadMessageAt
) {
    public static ReadStatusResponse from(ReadStatus readStatus) {
        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getUId(),
                readStatus.getCId(),
                readStatus.getCreatedAt(),
                readStatus.getModifiedAt(),
                readStatus.getLastReadMessageAt()
        );
    }
}
