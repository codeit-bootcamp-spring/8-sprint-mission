package com.sprint.mission.discodeit.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder //  빌더가 있어야 서비스에서 에러가 안 납니다.
public class UserStatusResponse {
    private UUID userId;
    private boolean isOnline;
    private Instant lastAccessAt;
}