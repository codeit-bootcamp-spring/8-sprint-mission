package com.sprint.mission.discodeit.dto.userstatus;


import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/*
    UserStatusCreateRequest
    -------------------------
    UserStatus 수정용 DTO (record)

    [필드 설명]
    • newLastActiveAt     : 유저의 마지막 접속 시간
 */
public record UserStatusUpdateRequest(

    @NotNull
    Instant newLastActiveAt
) {

}
