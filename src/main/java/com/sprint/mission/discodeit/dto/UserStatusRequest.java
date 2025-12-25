package com.sprint.mission.discodeit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class UserStatusRequest {
    private UUID userId;

    public UserStatusRequest(UUID userId) {
        this.userId = userId;
    }
}