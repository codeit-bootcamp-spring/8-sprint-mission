package com.sprint.mission.discodeit.dto.userstatus;


import java.time.Instant;
import java.util.UUID;

/*
    UserStatusCreateRequest
    -------------------------
    UserStatus 수정용 DTO (record)

    [필드 설명]
    • userId              : 수정 될 대상의 userId
    • newLastActiveAt     : 유저의 마지막 접속 시간
 */
public record UserStatusUpdateRequest(
    UUID userId,
    Instant newLastActiveAt
) {

}
