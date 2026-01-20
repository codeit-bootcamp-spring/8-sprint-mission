package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private UUID statusId;
    private UUID userId;
    private boolean online;
    private UUID profileId;
}