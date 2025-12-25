package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Getter //  필수: getId(), getUserId() 등을 생성합니다.
@NoArgsConstructor
public class UserStatus {
    private UUID id;
    private UUID userId;
    private Instant lastAccessAt;

    public UserStatus(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.lastAccessAt = Instant.now();
    }

    public boolean isOnline() {
        return lastAccessAt != null &&
                lastAccessAt.isAfter(Instant.now().minusSeconds(300));
    }

    public void updateLastAccessAt() {
        this.lastAccessAt = Instant.now();
    }
}