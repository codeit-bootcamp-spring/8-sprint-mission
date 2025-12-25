package com.sprint.mission.discodeit.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder //  빌더 패턴 사용 확인
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private UUID profileId;
    private boolean isOnline;
}