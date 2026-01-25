package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ReadStatusDto {
    private UUID id;
    private UUID userId;
    private UUID channelId;
    private Instant lastReadAt;
}
