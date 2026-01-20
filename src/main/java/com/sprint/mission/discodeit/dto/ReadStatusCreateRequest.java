package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReadStatusCreateRequest {
    private UUID userId;
    private UUID channelId;
    private Instant lastReadAt;
    // 하위 호환성을 위한 필드
    private UUID lastReadMessageId;
}