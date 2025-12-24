package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private UUID uId;
    private UserStatusType userStatusType;
    private final Instant createdAt;
    private Instant lastAccessAt;

    public UserStatus(UUID uId) {
        id = UUID.randomUUID();
        userStatusType = null;
        createdAt = Instant.now();
        lastAccessAt = createdAt;
        this.uId = uId;
    }

    public void update(UserStatusType userStatusType) {
        this.userStatusType = userStatusType;
    }

    public boolean isCurrentOnline() {
        return Duration.between(lastAccessAt, Instant.now()).toMinutes() <= 5;
    }

    public void updateLastAccess() {
        lastAccessAt = Instant.now();
        this.userStatusType = UserStatusType.ONLINE;
    }

    public UserStatusType getCurrentStatus() {
        return isCurrentOnline() ? UserStatusType.ONLINE : UserStatusType.OFFLINE;
    }
}
