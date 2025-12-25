package com.sprint.mission.discodeit.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserStatusResponse {
    private UUID userId;
    private boolean isOnline;
    private Instant lastAccessAt;
}