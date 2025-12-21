package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.util.UUID;

@Getter
public class UserStatus {
    private UUID id;
    private UUID userId;
    private boolean isOnline;

    public UserStatus() {}

    public UserStatus(UUID userId, boolean isOnline) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.isOnline = isOnline;
    }

    public void updateStatus(boolean isOnline) {
        this.isOnline = isOnline;
    }
}