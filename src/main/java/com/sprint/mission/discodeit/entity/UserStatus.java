package com.sprint.mission.discodeit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Getter //  필수: getId(), getUserId() 등을 생성합니다.
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserStatus {
    private UUID id;
    private UUID userId;
    private Instant lastAccessAt;

    public UserStatus(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.lastAccessAt = Instant.now();
    }

    @JsonIgnore // JSON 직렬화/역직렬화 시 제외 (계산된 값이므로 저장 불필요)
    public boolean isOnline() {
        return lastAccessAt != null &&
                lastAccessAt.isAfter(Instant.now().minusSeconds(300));
    }

    public void updateLastAccessAt() {
        this.lastAccessAt = Instant.now();
    }
}