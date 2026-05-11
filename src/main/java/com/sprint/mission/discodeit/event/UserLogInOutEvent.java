package com.sprint.mission.discodeit.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserLogInOutEvent {
    private final UUID userId;
    private final boolean online;

    public UserLogInOutEvent(UUID userId, boolean online) {
        this.userId = userId;
        this.online = online;
    }
}
