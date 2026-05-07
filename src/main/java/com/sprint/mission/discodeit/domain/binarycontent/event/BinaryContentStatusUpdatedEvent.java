package com.sprint.mission.discodeit.domain.binarycontent.event;

import com.sprint.mission.discodeit.domain.binarycontent.dto.domain.BinaryContentDto;

import java.time.Instant;

public record BinaryContentStatusUpdatedEvent
(
        String eventName,
        BinaryContentDto binaryContentDto,
        Instant createdAt
) {}
