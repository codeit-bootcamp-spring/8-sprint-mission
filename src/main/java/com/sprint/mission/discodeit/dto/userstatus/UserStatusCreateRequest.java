package com.sprint.mission.discodeit.dto.userstatus;

import java.time.Instant;
import java.util.UUID;

/*
    UserStatusCreateRequest
    -------------------------
    UserStatus 추가 시 넘겨주는 데이터를 모아놓은 DTO (record)

    [필드 설명]
    • userId              : 유저 id
    • lastConnAt          : 유저의 마지막 접속 시간
 */
public record UserStatusCreateRequest(
        UUID userId,
        Instant lastConnAt
) {
}
