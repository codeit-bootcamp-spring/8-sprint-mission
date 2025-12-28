package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter //  JSON으로 변환될 때 이 어노테이션이 있어야 값이 담깁니다.
@NoArgsConstructor
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