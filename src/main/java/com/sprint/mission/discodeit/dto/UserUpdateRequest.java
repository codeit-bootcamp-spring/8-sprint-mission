package com.sprint.mission.discodeit.dto;

import lombok.Getter;
import java.util.UUID;

@Getter
public class UserUpdateRequest {
    private UUID id;
    private String name;
    private String password; //  필드가 있는지 확인하세요.
}